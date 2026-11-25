package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {

    /** Che token để không lọt vào log. */
    @Override
    public String toString() {
        return "RefreshRequest[]";
    }
}
