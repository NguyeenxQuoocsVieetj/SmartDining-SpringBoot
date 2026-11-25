package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDateTime;

/** Lịch đặt như khách nhìn thấy, không có bàn và người xác nhận. */
public record PublicReservationResponse(
        Long id,
        String customerName,
        String phone,
        LocalDateTime arrivalTime,
        ReservationStatus status,
        LocalDateTime createdAt) {

    public static PublicReservationResponse from(Reservation reservation) {
        return new PublicReservationResponse(reservation.getId(), reservation.getCustomerName(),
                reservation.getPhone(), reservation.getArrivalTime(), reservation.getStatus(),
                reservation.getCreatedAt());
    }
}
