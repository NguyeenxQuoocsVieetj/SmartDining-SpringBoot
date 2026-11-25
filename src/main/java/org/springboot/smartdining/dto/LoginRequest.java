package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {

    /** Che mật khẩu để không lọt vào log. */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
