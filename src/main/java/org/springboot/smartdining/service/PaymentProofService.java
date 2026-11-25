package org.springboot.smartdining.service;

import org.springboot.smartdining.storage.PaymentProofStorage;

/** Quản trị xem ảnh bill của khoản thu. */
public interface PaymentProofService {

    /** Không có khoản thu: 404 NOT_FOUND; không có ảnh đọc được: 404 NO_PROOF. */
    PaymentProofStorage.StoredImage getProof(Long paymentId);
}
