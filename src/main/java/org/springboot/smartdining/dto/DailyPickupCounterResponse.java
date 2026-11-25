package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.DailyPickupCounter;

import java.time.LocalDate;

public record DailyPickupCounterResponse(
        LocalDate businessDate,
        Integer lastCode) {

    public static DailyPickupCounterResponse from(DailyPickupCounter counter) {
        return new DailyPickupCounterResponse(counter.getBusinessDate(), counter.getLastCode());
    }
}
