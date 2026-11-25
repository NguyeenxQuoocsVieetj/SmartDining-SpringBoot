package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.enums.PaymentStatus;
import org.springboot.smartdining.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Ghi khoản thu đã hoàn tất vào ca đang mở, dùng chung cho thanh toán tại bàn và bán mang về. */
@Component
@RequiredArgsConstructor
class PaymentRecorder {

    private final PaymentMapper paymentMapper;

    /** @param shift ca đã khóa bằng {@link OpenShiftLocker} */
    Payment record(Long orderId, CounterShift shift, PaymentMethod method, BigDecimal amount,
                   String proofPath, LocalDateTime paidAt) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCounterShiftId(shift.getId());
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProofImagePath(proofPath);
        payment.setPaidAt(paidAt);
        paymentMapper.insert(payment);
        return payment;
    }
}
