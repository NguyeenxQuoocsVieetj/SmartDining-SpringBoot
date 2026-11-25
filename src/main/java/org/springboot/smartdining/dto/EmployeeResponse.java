package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String username,
        String fullName,
        String phone,
        String email,
        String avatarUrl,
        EmployeeRole role,
        EmployeeStatus status,
        LocalDateTime createdAt) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getUsername(), employee.getFullName(),
                employee.getPhone(), employee.getEmail(), employee.getAvatarUrl(), employee.getRole(),
                employee.getStatus(), employee.getCreatedAt());
    }
}
