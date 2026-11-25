package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.CustomerRequestResponse;

/** Khách tại bàn yêu cầu thanh toán hoặc gọi nhân viên. */
public interface CustomerRequestService {

    /** @param created false khi bàn đã có sẵn yêu cầu thanh toán đang chờ */
    record PaymentRequestResult(CustomerRequestResponse request, boolean created) {
    }

    PaymentRequestResult requestPayment(String token);

    /** Gọi nhân viên, được gửi nhiều lần. Bàn đang trống thì từ chối. */
    CustomerRequestResponse callService(String token, String serviceType, String note);
}
