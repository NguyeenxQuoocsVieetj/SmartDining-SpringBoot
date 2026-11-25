package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;

public record OpenTableSessionRequest(@NotBlank String qrToken) {
}
