package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springboot.smartdining.enums.CodeChannel;

public record SendCodeRequest(
        @NotBlank String username,
        @NotNull CodeChannel channel) {
}
