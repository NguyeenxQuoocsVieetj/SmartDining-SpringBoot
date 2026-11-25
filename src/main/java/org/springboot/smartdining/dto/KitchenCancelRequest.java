package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KitchenCancelRequest(@NotBlank @Size(max = 255) String reason) {

    /** Bỏ khoảng trắng hai đầu trước khi kiểm độ dài. */
    public KitchenCancelRequest {
        reason = reason == null ? null : reason.strip();
    }
}
