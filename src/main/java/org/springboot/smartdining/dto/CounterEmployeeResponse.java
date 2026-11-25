package org.springboot.smartdining.dto;

/** Nhân viên có thể vào quầy; email, số điện thoại đã che. */
public record CounterEmployeeResponse(
        Long employeeId,
        String username,
        String fullName,
        String maskedEmail,
        String maskedPhone) {
}
