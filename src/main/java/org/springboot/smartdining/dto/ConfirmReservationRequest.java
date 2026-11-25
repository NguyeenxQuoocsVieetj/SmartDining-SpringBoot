package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmReservationRequest(@NotNull Long tableId) {
}
