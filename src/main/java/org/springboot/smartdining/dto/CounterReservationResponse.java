package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * Một lịch đặt trên màn hình quầy.
 * Thứ tự thành phần phải khớp resultMap trong ReservationMapper.xml.
 */
public record CounterReservationResponse(
        Long id,
        String customerName,
        String phone,
        LocalDateTime arrivalTime,
        Long tableId,
        String tableName,
        ReservationStatus status,
        Long confirmedBy,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt) {
}
