package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.EmployeeRole;

public record EmployeeDetailResponse(
        Long employeeId,
        String fullName,
        String username,
        EmployeeRole role) {
}
