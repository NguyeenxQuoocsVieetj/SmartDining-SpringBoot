package org.springboot.smartdining.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Nhân viên tự đăng ký (ACC-1). Vai trò và trạng thái do server đặt. */
public record RegisterRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,50}",
                message = "dài 3-50 ký tự, chỉ gồm A-Z a-z 0-9 . _ -") String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 15) String phone) {

    /** Che mật khẩu để không lọt vào log. */
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + "]";
    }
}
