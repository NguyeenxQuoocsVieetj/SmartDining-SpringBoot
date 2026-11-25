package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long id,
        Long paymentId,
        Long orderItemId,
        Long counterShiftId,
        BigDecimal amount,
        String reason,
        LocalDateTime refundedAt) {

    public static RefundResponse from(Refund refund) {
        return new RefundResponse(refund.getId(), refund.getPaymentId(), refund.getOrderItemId(),
                refund.getCounterShiftId(), refund.getAmount(), refund.getReason(), refund.getRefundedAt());
    }
}
