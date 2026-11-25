package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Số tiền do server tính. reason bỏ trống thì lấy lý do hủy món. */
public record CounterRefundRequest(
        @NotNull Long orderItemId,
        @Size(max = 255) String reason) {

    public CounterRefundRequest {
        reason = reason == null || reason.isBlank() ? null : reason.strip();
    }
}
