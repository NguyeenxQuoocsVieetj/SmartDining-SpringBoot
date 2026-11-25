package org.springboot.smartdining.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

/** password bắt buộc khi tạo mới; khi cập nhật, để trống (null) là giữ mật khẩu. */
public record EmployeeRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,50}",
                message = "dài 3-50 ký tự, chỉ gồm A-Z a-z 0-9 . _ -") String username,
        @NotBlank(groups = OnCreate.class) @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 15) String phone,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 500) String avatarUrl,
        EmployeeRole role,
        EmployeeStatus status) {

    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setFullName(fullName);
        employee.setPhone(phone);
        employee.setEmail(email);
        employee.setAvatarUrl(avatarUrl);
        employee.setRole(role);
        employee.setStatus(status);
        return employee;
    }

    /** Che mật khẩu để không lọt vào log. */
    @Override
    public String toString() {
        return "EmployeeRequest[username=" + username + "]";
    }
}
