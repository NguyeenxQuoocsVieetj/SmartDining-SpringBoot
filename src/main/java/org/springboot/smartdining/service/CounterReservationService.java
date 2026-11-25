package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.CounterReservationResponse;
import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

/** Quầy xử lý lịch đặt bàn. */
public interface CounterReservationService {

    /**
     * Tra lịch theo ngày đến, trạng thái, số điện thoại; tham số null hoặc rỗng thì không lọc.
     * Giờ đến sớm nhất trước.
     */
    List<CounterReservationResponse> search(LocalDate date, ReservationStatus status, String phone);
}
