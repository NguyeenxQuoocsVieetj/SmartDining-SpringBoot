package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Employee;

import java.time.LocalDateTime;

/** Ca quầy đang mở cùng người trực. */
public record CounterSessionResponse(
        Long id,
        Long employeeId,
        String username,
        String fullName,
        LocalDateTime startedAt) {

    public static CounterSessionResponse from(CounterShift shift, Employee employee) {
        return new CounterSessionResponse(shift.getId(), employee.getId(), employee.getUsername(),
                employee.getFullName(), shift.getStartedAt());
    }
}
