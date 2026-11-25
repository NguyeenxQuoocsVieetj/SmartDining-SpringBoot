package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.PaymentMapper;
import org.springboot.smartdining.service.PaymentProofService;
import org.springboot.smartdining.storage.PaymentProofStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentProofServiceImpl implements PaymentProofService {

    private final PaymentMapper paymentMapper;
    private final PaymentProofStorage paymentProofStorage;

    @Override
    public PaymentProofStorage.StoredImage getProof(Long paymentId) {
        Payment payment = paymentMapper.findById(paymentId);
        if (payment == null) {
            throw new ResourceNotFoundException("Không tìm thấy khoản thu " + paymentId);
        }
        // Thông báo không nhắc tới đường dẫn trên máy chủ
        return paymentProofStorage.load(payment.getProofImagePath())
                .orElseThrow(() -> new ResourceNotFoundException("NO_PROOF", "Khoản thu không có ảnh bill"));
    }
}
