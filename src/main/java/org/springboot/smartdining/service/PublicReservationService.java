package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.PublicReservationRequest;
import org.springboot.smartdining.dto.PublicReservationResponse;

/** Khách đặt bàn, không đăng nhập. */
public interface PublicReservationService {

    /** Tạo lịch chờ xác nhận; mỗi số điện thoại chỉ có một lịch đang chờ (RES-4). */
    PublicReservationResponse create(PublicReservationRequest request);
}
