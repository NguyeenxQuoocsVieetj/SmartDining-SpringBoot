package org.springboot.smartdining.security;

import org.springboot.smartdining.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mã vào quầy, giữ trong bộ nhớ ứng dụng (CNT-4): khởi động lại là mất, và chỉ đúng khi chạy một máy chủ.
 * Mọi thao tác đồng bộ hóa trên cùng một khóa, nên gửi mã và thử mã là nguyên tử.
 */
@Component
public class CounterCodeStore {

    private static final SecureRandom RANDOM = new SecureRandom();

    public record IssuedCode(String code, LocalDateTime expiresAt) {
    }

    /** Kết quả thử một mã; UNAVAILABLE là chưa gửi, hết hạn, đã dùng hoặc đã bị hủy. */
    public enum Verification {
        ACCEPTED,
        WRONG,
        UNAVAILABLE
    }

    private static final class Entry {
        /** null khi mã đã dùng hoặc đã bị hủy; mục vẫn được giữ để tính thời gian chờ gửi lại. */
        private String code;
        private int failures;
        private final Instant sentAt;
        private final Instant expiresAt;

        private Entry(String code, Instant sentAt, Instant expiresAt) {
            this.code = code;
            this.sentAt = sentAt;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<Long, Entry> entries = new HashMap<>();
    private final Duration timeToLive;
    private final Duration resendDelay;
    private final int maxAttempts;

    public CounterCodeStore(@Value("${smartdining.counter-code.ttl:5m}") Duration timeToLive,
                            @Value("${smartdining.counter-code.resend-delay:60s}") Duration resendDelay,
                            @Value("${smartdining.counter-code.max-attempts:5}") int maxAttempts) {
        if (timeToLive.isZero() || timeToLive.isNegative() || resendDelay.isNegative() || maxAttempts < 1) {
            throw new IllegalStateException("smartdining.counter-code: ttl phải lớn hơn 0, "
                    + "resend-delay không được âm, max-attempts ít nhất 1");
        }
        this.timeToLive = timeToLive;
        this.resendDelay = resendDelay;
        this.maxAttempts = maxAttempts;
    }

    public Duration resendDelay() {
        return resendDelay;
    }

    /** Phát mã mới thay mã cũ; gửi lại cho cùng người quá sớm thì 429. */
    public synchronized IssuedCode issue(Long employeeId) {
        Instant now = Instant.now();
        Entry current = entries.get(employeeId);
        if (current != null && now.isBefore(current.sentAt.plus(resendDelay))) {
            throw new TooManyRequestsException("CODE_RECENTLY_SENT",
                    "Vừa gửi mã, hãy đợi " + resendDelay.toSeconds() + " giây rồi gửi lại");
        }
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        Instant expiresAt = now.plus(timeToLive);
        entries.put(employeeId, new Entry(code, now, expiresAt));
        return new IssuedCode(code,
                LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS));
    }

    /** Thử một mã. Đúng thì mã hết tác dụng ngay; sai đủ số lần thì mã bị hủy (CNT-4). */
    public synchronized Verification verify(Long employeeId, String code) {
        Entry entry = entries.get(employeeId);
        if (entry == null || entry.code == null || !Instant.now().isBefore(entry.expiresAt)) {
            return Verification.UNAVAILABLE;
        }
        // So sánh thời gian không đổi, không lộ đã đúng được mấy chữ số
        if (!MessageDigest.isEqual(entry.code.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8))) {
            entry.failures++;
            if (entry.failures >= maxAttempts) {
                entry.code = null;
            }
            return Verification.WRONG;
        }
        entry.code = null;
        return Verification.ACCEPTED;
    }

    /** Bỏ mọi mã đang chờ, dùng khi đã mở ca. */
    public synchronized void clear() {
        entries.clear();
    }
}
