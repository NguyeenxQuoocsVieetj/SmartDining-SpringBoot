package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.CounterShift;

import java.time.LocalDateTime;

public record CounterShiftResponse(
        Long id,
        Long employeeId,
        LocalDateTime startedAt,
        LocalDateTime endedAt) {

    public static CounterShiftResponse from(CounterShift shift) {
        return new CounterShiftResponse(shift.getId(), shift.getEmployeeId(), shift.getStartedAt(),
                shift.getEndedAt());
    }
}
