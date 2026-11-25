package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * phone được phép dài hơn cột (15) vì còn khoảng trắng, dấu chấm, gạch;
 * service chuẩn hóa rồi mới kiểm độ dài.
 */
public record ReservationRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Size(max = 30) String phone,
        @NotNull LocalDateTime arrivalTime,
        Long tableId,
        ReservationStatus status) {

    public Reservation toEntity() {
        Reservation reservation = new Reservation();
        reservation.setCustomerName(customerName);
        reservation.setPhone(phone);
        reservation.setArrivalTime(arrivalTime);
        reservation.setTableId(tableId);
        reservation.setStatus(status);
        return reservation;
    }
}
