package org.springboot.smartdining.exception;

/** Thiếu token phiên bàn, token sai, hết hạn hoặc phiên đã đóng. Không đưa token vào thông báo. */
public class TableSessionInvalidException extends RuntimeException {

    public TableSessionInvalidException() {
        super("Phiên bàn không hợp lệ hoặc đã hết hạn, hãy quét lại mã QR");
    }
}
