package org.springboot.smartdining.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Không có giá: server lấy giá hiện tại của món. */
public record AddCartItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) @Max(100) Integer quantity,
        @Size(max = 255) String note) {
}
