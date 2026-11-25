package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        String customerName,
        String phone,
        LocalDateTime arrivalTime,
        Long tableId,
        ReservationStatus status,
        Long confirmedBy,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getCustomerName(), reservation.getPhone(),
                reservation.getArrivalTime(), reservation.getTableId(), reservation.getStatus(),
                reservation.getConfirmedBy(), reservation.getConfirmedAt(), reservation.getCreatedAt());
    }
}
