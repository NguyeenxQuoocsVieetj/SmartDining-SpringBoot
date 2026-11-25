package org.springboot.smartdining.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Refund;

import java.math.BigDecimal;

public record RefundRequest(
        @NotNull Long paymentId,
        Long orderItemId,
        @NotNull Long counterShiftId,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 11, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 255) String reason) {

    public Refund toEntity() {
        Refund refund = new Refund();
        refund.setPaymentId(paymentId);
        refund.setOrderItemId(orderItemId);
        refund.setCounterShiftId(counterShiftId);
        refund.setAmount(amount);
        refund.setReason(reason);
        return refund;
    }
}
