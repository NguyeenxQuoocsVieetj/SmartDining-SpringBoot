package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long counterShiftId,
        PaymentMethod method,
        BigDecimal amount,
        PaymentStatus status,
        String proofImagePath,
        LocalDateTime paidAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getCounterShiftId(),
                payment.getMethod(), payment.getAmount(), payment.getStatus(), payment.getProofImagePath(),
                payment.getPaidAt());
    }
}
