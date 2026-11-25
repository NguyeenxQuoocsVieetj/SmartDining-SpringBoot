package org.springboot.smartdining.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springboot.smartdining.entity.DailyPickupCounter;

import java.time.LocalDate;

/** businessDate bắt buộc khi tạo mới; khi cập nhật ngày lấy từ đường dẫn. */
public record DailyPickupCounterRequest(
        @NotNull(groups = OnCreate.class) LocalDate businessDate,
        @NotNull @Min(0) Integer lastCode) {

    public DailyPickupCounter toEntity() {
        return new DailyPickupCounter(businessDate, lastCode);
    }
}
