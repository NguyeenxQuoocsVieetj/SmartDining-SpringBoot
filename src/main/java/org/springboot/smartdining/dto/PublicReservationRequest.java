package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Khách tự đặt bàn (RES-1, RES-2): không chọn bàn, không đặt món. */
public record PublicReservationRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank String phone,
        @NotNull LocalDateTime arrivalTime) {

    public PublicReservationRequest {
        customerName = customerName == null ? null : customerName.strip();
    }
}
