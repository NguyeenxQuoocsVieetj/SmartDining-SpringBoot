package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireMoney;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CounterShiftMapper counterShiftMapper;

    public Payment create(Payment input) {
        Payment payment = fromInput(input);
        payment.setPaidAt(orNow(input.getPaidAt()));
        paymentMapper.insert(payment);
        return findExisting(payment.getId());
    }

    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAll() {
        return paymentMapper.findAll();
    }

    public Payment update(Long id, Payment input) {
        findExisting(id);
        Payment payment = fromInput(input);
        payment.setId(id);
        paymentMapper.update(payment);
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        paymentMapper.deleteById(id);
    }

    private Payment findExisting(Long id) {
        Payment payment = paymentMapper.findById(id);
        if (payment == null) {
            throw new ResourceNotFoundException("Không tìm thấy khoản thu " + id);
        }
        return payment;
    }

    private Payment fromInput(Payment input) {
        require(input, "dữ liệu khoản thu");
        Payment payment = new Payment();
        payment.setOrderId(requireReference(input.getOrderId(), orderMapper::findById, "orderId"));
        payment.setCounterShiftId(
                requireReference(input.getCounterShiftId(), counterShiftMapper::findById, "counterShiftId"));
        payment.setMethod(require(input.getMethod(), "method"));
        payment.setAmount(requireMoney(input.getAmount(), "amount"));
        payment.setStatus(input.getStatus());
        payment.setProofImagePath(payment.getMethod() == PaymentMethod.TRANSFER
                ? requireText(input.getProofImagePath(), "proofImagePath cho khoản chuyển khoản", 500)
                : optionalText(input.getProofImagePath(), "proofImagePath", 500));
        return payment;
    }
}
