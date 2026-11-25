package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceCallRequest(
        @NotBlank @Size(max = 80) String serviceType,
        @Size(max = 255) String note) {
}
