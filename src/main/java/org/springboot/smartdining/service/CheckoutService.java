package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.CheckoutResponse;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springframework.web.multipart.MultipartFile;

/** Quầy thu tiền của bàn. */
public interface CheckoutService {

    /**
     * Thanh toán đơn đang mở của bàn (PAY-1 đến PAY-4): xóa món còn trong giỏ, chốt đơn,
     * ghi khoản thu vào ca đang mở, đóng yêu cầu và phiên của bàn, trả bàn về trống.
     *
     * @param proof ảnh bill, bắt buộc khi chuyển khoản, bỏ qua khi tiền mặt
     */
    CheckoutResponse checkout(Long tableId, PaymentMethod method, MultipartFile proof);
}
