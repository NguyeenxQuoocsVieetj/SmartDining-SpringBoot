package org.springboot.smartdining.storage;

import lombok.extern.slf4j.Slf4j;
import org.springboot.smartdining.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/** Lưu ảnh bill chuyển khoản (PAY-3) ra đĩa; database chỉ giữ đường dẫn tương đối. */
@Slf4j
@Component
public class PaymentProofStorage {

    private static final long MAX_BYTES = 3L * 1024 * 1024;
    private static final String FOLDER = "payment-proofs";
    private static final DateTimeFormatter MONTH_FOLDER = DateTimeFormatter.ofPattern("yyyy/MM");

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private final Path root;

    public PaymentProofStorage(@Value("${smartdining.payment-proof.dir:./data}") Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    /**
     * Kiểm ảnh rồi ghi xuống đĩa với tên do server đặt. Nếu transaction đang chạy bị rollback
     * thì file được xóa. Phải gọi bên trong transaction.
     *
     * @return đường dẫn tương đối để lưu vào database, ví dụ payment-proofs/2026/11/xxx.jpg
     */
    public String store(MultipartFile proof) {
        if (proof == null || proof.isEmpty()) {
            throw new BadRequestException("PROOF_REQUIRED", "Chuyển khoản phải có ảnh bill");
        }
        if (proof.getSize() > MAX_BYTES) {
            throw invalid("Ảnh bill tối đa 3MB");
        }
        byte[] content = read(proof);
        String relativePath = FOLDER + "/" + LocalDate.now().format(MONTH_FOLDER) + "/"
                + UUID.randomUUID() + "." + extensionOf(content);
        Path target = root.resolve(relativePath);

        // Đăng ký xóa trước khi ghi, để file ghi dở cũng được dọn
        deleteIfRolledBack(target);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Không lưu được ảnh bill", e);
        }
        return relativePath;
    }

    private static byte[] read(MultipartFile proof) {
        try {
            return proof.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được ảnh bill", e);
        }
    }

    /** Nhận dạng theo byte đầu file, không tin Content-Type hay tên file do client gửi. */
    private static String extensionOf(byte[] content) {
        if (startsWith(content, 0, JPEG)) {
            return "jpg";
        }
        if (startsWith(content, 0, PNG)) {
            return "png";
        }
        if (startsWith(content, 0, RIFF) && startsWith(content, 8, WEBP)) {
            return "webp";
        }
        throw invalid("Ảnh bill phải là JPEG, PNG hoặc WEBP");
    }

    private static boolean startsWith(byte[] content, int offset, byte[] signature) {
        return content.length >= offset + signature.length
                && Arrays.equals(content, offset, offset + signature.length, signature, 0, signature.length);
    }

    private static BadRequestException invalid(String message) {
        return new BadRequestException("INVALID_PROOF", message);
    }

    private static void deleteIfRolledBack(Path file) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // Không rõ kết quả commit thì giữ file: thà thừa file còn hơn mất ảnh của khoản đã thu
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        log.warn("Không xóa được ảnh bill của giao dịch đã hủy: {}", file, e);
                    }
                }
            }
        });
    }
}
