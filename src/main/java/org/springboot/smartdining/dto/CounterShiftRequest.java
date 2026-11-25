package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;
import org.springboot.smartdining.entity.CounterShift;

import java.time.LocalDateTime;

public record CounterShiftRequest(
        @NotNull Long employeeId,
        LocalDateTime endedAt) {

    public CounterShift toEntity() {
        CounterShift shift = new CounterShift();
        shift.setEmployeeId(employeeId);
        shift.setEndedAt(endedAt);
        return shift;
    }
}
