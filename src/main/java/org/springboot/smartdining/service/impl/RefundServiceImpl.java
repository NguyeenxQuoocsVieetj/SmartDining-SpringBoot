package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Refund;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.PaymentMapper;
import org.springboot.smartdining.mapper.RefundMapper;
import org.springboot.smartdining.service.RefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.optionalReference;
import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireMoney;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundMapper refundMapper;
    private final PaymentMapper paymentMapper;
    private final OrderItemMapper orderItemMapper;
    private final CounterShiftMapper counterShiftMapper;

    @Override
    public Refund create(Refund input) {
        Refund refund = fromInput(input);
        refund.setRefundedAt(orNow(input.getRefundedAt()));
        refundMapper.insert(refund);
        return findExisting(refund.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Refund getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> getAll() {
        return refundMapper.findAll();
    }

    @Override
    public Refund update(Long id, Refund input) {
        findExisting(id);
        Refund refund = fromInput(input);
        refund.setId(id);
        refundMapper.update(refund);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        findExisting(id);
        refundMapper.deleteById(id);
    }

    private Refund findExisting(Long id) {
        Refund refund = refundMapper.findById(id);
        if (refund == null) {
            throw new ResourceNotFoundException("Không tìm thấy khoản hoàn " + id);
        }
        return refund;
    }

    private Refund fromInput(Refund input) {
        require(input, "dữ liệu khoản hoàn");
        Refund refund = new Refund();
        refund.setPaymentId(requireReference(input.getPaymentId(), paymentMapper::findById, "paymentId"));
        refund.setOrderItemId(optionalReference(input.getOrderItemId(), orderItemMapper::findById, "orderItemId"));
        refund.setCounterShiftId(
                requireReference(input.getCounterShiftId(), counterShiftMapper::findById, "counterShiftId"));
        refund.setAmount(requireMoney(input.getAmount(), "amount"));
        if (refund.getAmount().signum() == 0) {
            throw new BadRequestException("amount hoàn phải lớn hơn 0");
        }
        refund.setReason(requireText(input.getReason(), "reason", 255));
        return refund;
    }
}
