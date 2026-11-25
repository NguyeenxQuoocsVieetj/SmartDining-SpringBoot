package org.springboot.smartdining.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull Long orderId,
        @NotNull Long counterShiftId,
        @NotNull PaymentMethod method,
        @NotNull @DecimalMin("0") @Digits(integer = 11, fraction = 2) BigDecimal amount,
        PaymentStatus status,
        @Size(max = 500) String proofImagePath) {

    public Payment toEntity() {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCounterShiftId(counterShiftId);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setProofImagePath(proofImagePath);
        return payment;
    }
}
