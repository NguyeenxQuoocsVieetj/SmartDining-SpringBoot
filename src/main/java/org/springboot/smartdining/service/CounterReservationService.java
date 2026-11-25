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

    /**
     * Xác nhận lịch đang chờ và xếp bàn (RES-3), ghi người đang trực quầy. Hai lịch đã xác nhận
     * cùng một bàn phải cách nhau đủ khoảng quy định. Không đổi trạng thái bàn: bàn không bị khóa
     * trước giờ đến, đang có khách vẫn xếp được.
     */
    CounterReservationResponse confirm(Long id, Long tableId);

    /** Khách đã đến: chỉ lịch đã xác nhận. */
    CounterReservationResponse arrive(Long id);

    /** Hủy lịch đang chờ hoặc đã xác nhận; giữ bàn và người xác nhận đã ghi. */
    CounterReservationResponse cancel(Long id);
}
