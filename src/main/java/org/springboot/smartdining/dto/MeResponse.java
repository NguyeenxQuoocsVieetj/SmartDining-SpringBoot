package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

/** Thông tin người đang đăng nhập. */
public record MeResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        EmployeeRole role,
        EmployeeStatus status,
        boolean onCounterShift) {
}
