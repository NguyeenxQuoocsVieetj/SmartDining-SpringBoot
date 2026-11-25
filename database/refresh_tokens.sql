-- SmartDining M8: bảng phiên đăng nhập (refresh token).
-- Chạy một lần trên database đã có bảng employees. Chỉ tạo bảng mới, không đụng dữ liệu cũ.
-- Database chỉ giữ SHA-256 của refresh token; giờ do ứng dụng gán.

CREATE TABLE refresh_tokens (
    id           BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id  BIGINT   NOT NULL,
    session_id   CHAR(36) NOT NULL,
    token_hash   CHAR(64) NOT NULL,
    created_at   DATETIME NOT NULL,
    expires_at   DATETIME NOT NULL,
    revoked_at   DATETIME,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    INDEX ix_refresh_tokens_session (session_id),
    INDEX ix_refresh_tokens_employee (employee_id)
);
