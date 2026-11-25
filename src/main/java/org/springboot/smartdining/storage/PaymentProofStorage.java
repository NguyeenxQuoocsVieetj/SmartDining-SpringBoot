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
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/** Lưu và đọc ảnh bill chuyển khoản (PAY-3) trên đĩa; database chỉ giữ đường dẫn tương đối. */
@Slf4j
@Component
public class PaymentProofStorage {

    private static final long MAX_BYTES = 3L * 1024 * 1024;
    private static final String FOLDER = "payment-proofs";
    private static final DateTimeFormatter MONTH_FOLDER = DateTimeFormatter.ofPattern("yyyy/MM");

    /** Nội dung một ảnh đã đọc và kiểu của nó. */
    public record StoredImage(byte[] content, String contentType) {
    }

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
        ImageType type = ImageType.detect(content)
                .orElseThrow(() -> invalid("Ảnh bill phải là JPEG, PNG hoặc WEBP"));
        String relativePath = FOLDER + "/" + LocalDate.now().format(MONTH_FOLDER) + "/"
                + UUID.randomUUID() + "." + type.extension();
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

    /**
     * Đọc ảnh theo đường dẫn lưu trong database. Đường dẫn đó có thể do API quản trị nhập tùy ý, nên
     * chỉ đọc file ảnh thật sự nằm trong thư mục ảnh bill, kể cả sau khi đi theo liên kết tượng trưng.
     *
     * @return rỗng khi không có ảnh hợp lệ để trả
     */
    public Optional<StoredImage> load(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return Optional.empty();
        }
        Path folder = root.resolve(FOLDER);
        try {
            Path relative = Path.of(storedPath);
            // Có gốc nghĩa là đường dẫn tuyệt đối, ổ đĩa hoặc máy khác trong mạng: không đụng tới
            if (relative.getRoot() != null) {
                return Optional.empty();
            }
            Path target = root.resolve(relative).normalize();
            if (!target.startsWith(folder)) {
                return Optional.empty();
            }
            Path real = target.toRealPath();
            if (!real.startsWith(folder.toRealPath()) || !Files.isRegularFile(real)
                    || Files.size(real) > MAX_BYTES) {
                return Optional.empty();
            }
            byte[] content = Files.readAllBytes(real);
            return ImageType.detect(content).map(type -> new StoredImage(content, type.contentType()));
        } catch (InvalidPathException | NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            log.warn("Không đọc được ảnh bill: {}", storedPath, e);
            return Optional.empty();
        }
    }

    private static byte[] read(MultipartFile proof) {
        try {
            return proof.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được ảnh bill", e);
        }
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
