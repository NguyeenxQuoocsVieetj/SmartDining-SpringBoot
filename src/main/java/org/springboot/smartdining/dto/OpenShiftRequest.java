package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OpenShiftRequest(
        @NotBlank String username,
        @NotNull @Pattern(regexp = "[0-9]{6}", message = "phải gồm đúng 6 chữ số") String code) {

    /** Che mã để không lọt vào log. */
    @Override
    public String toString() {
        return "OpenShiftRequest[username=" + username + "]";
    }
}
