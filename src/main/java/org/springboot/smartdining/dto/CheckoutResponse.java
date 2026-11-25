package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Kết quả thanh toán của bàn. Không trả đường dẫn ảnh bill. */
public record CheckoutResponse(
        Long paymentId,
        Long orderId,
        Long tableId,
        String tableName,
        PaymentMethod method,
        BigDecimal amount,
        LocalDateTime paidAt,
        Long counterShiftId,
        Long cashierId,
        boolean hasProof) {
}
