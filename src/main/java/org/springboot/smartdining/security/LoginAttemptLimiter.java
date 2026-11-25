package org.springboot.smartdining.security;

import org.springboot.smartdining.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Chặn dò mật khẩu theo username và theo địa chỉ IP, giữ trong bộ nhớ (một máy chủ).
 * Mỗi lần thử được tính ngay trước khi kiểm mật khẩu, nên nhiều request song song cũng không thử
 * quá số lần cho phép; đăng nhập đúng thì trả lại lượt.
 */
@Component
public class LoginAttemptLimiter {

    /** Quá số mục này thì dọn các mục đã hết hạn, để không phình bộ nhớ. */
    private static final int PURGE_THRESHOLD = 10_000;

    /** Một lần thử đã được tính. */
    public record Attempt(String username, String address, Instant at) {
    }

    private static final class History {
        private final Deque<Instant> attempts = new ArrayDeque<>();
        private Instant lockedUntil;
    }

    private final Map<String, History> byUsername = new HashMap<>();
    private final Map<String, History> byAddress = new HashMap<>();
    private final int maxPerUsername;
    private final int maxPerAddress;
    private final Duration window;
    private final Duration lockTime;

    public LoginAttemptLimiter(
            @Value("${smartdining.security.login.max-failures-per-username:5}") int maxPerUsername,
            @Value("${smartdining.security.login.max-failures-per-ip:20}") int maxPerAddress,
            @Value("${smartdining.security.login.window:15m}") Duration window,
            @Value("${smartdining.security.login.lock:15m}") Duration lockTime) {
        if (maxPerUsername < 1 || maxPerAddress < 1 || window.isNegative() || window.isZero()
                || lockTime.isNegative() || lockTime.isZero()) {
            throw new IllegalStateException("smartdining.security.login: các giới hạn phải lớn hơn 0");
        }
        this.maxPerUsername = maxPerUsername;
        this.maxPerAddress = maxPerAddress;
        this.window = window;
        this.lockTime = lockTime;
    }

    /**
     * Tính một lần thử; đang bị khóa thì 429.
     *
     * @param username đã chuẩn hóa, null nếu không thể là username hợp lệ (chỉ tính theo IP)
     */
    public synchronized Attempt acquire(String username, String address) {
        Instant now = Instant.now();
        requireNotLocked(byAddress, address, now);
        if (username != null) {
            requireNotLocked(byUsername, username, now);
            record(byUsername, username, maxPerUsername, now);
        }
        record(byAddress, address, maxPerAddress, now);
        return new Attempt(username, address, now);
    }

    /** Mật khẩu đúng: xóa số đếm của username, và không tính lần này cho IP. */
    public synchronized void succeed(Attempt attempt) {
        if (attempt.username() != null) {
            byUsername.remove(attempt.username());
        }
        History history = byAddress.get(attempt.address());
        if (history != null) {
            history.attempts.removeLastOccurrence(attempt.at());
            if (history.attempts.size() < maxPerAddress) {
                history.lockedUntil = null;
            }
        }
    }

    private void requireNotLocked(Map<String, History> histories, String key, Instant now) {
        History history = histories.get(key);
        if (history == null) {
            return;
        }
        if (history.lockedUntil != null) {
            if (now.isBefore(history.lockedUntil)) {
                throw new TooManyRequestsException("TOO_MANY_ATTEMPTS",
                        "Thử sai quá nhiều lần, hãy đợi rồi thử lại");
            }
            histories.remove(key);
        }
    }

    private void record(Map<String, History> histories, String key, int max, Instant now) {
        if (histories.size() >= PURGE_THRESHOLD) {
            histories.values().removeIf(history -> isStale(history, now));
        }
        History history = histories.computeIfAbsent(key, ignored -> new History());
        Instant oldest = now.minus(window);
        while (!history.attempts.isEmpty() && !history.attempts.peekFirst().isAfter(oldest)) {
            history.attempts.removeFirst();
        }
        history.attempts.addLast(now);
        if (history.attempts.size() >= max) {
            history.lockedUntil = now.plus(lockTime);
        }
    }

    private boolean isStale(History history, Instant now) {
        boolean unlocked = history.lockedUntil == null || !now.isBefore(history.lockedUntil);
        return unlocked && (history.attempts.isEmpty()
                || !history.attempts.peekLast().isAfter(now.minus(window)));
    }
}
