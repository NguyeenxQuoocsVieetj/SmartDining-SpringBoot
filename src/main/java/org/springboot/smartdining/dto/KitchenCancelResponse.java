package org.springboot.smartdining.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dòng món sau khi hủy, các trường của món nằm cùng cấp với những trường dưới đây.
 *
 * @param refundAmount số tiền cần trả lại khách khi đơn mang về đã trả tiền, ngược lại null
 */
public record KitchenCancelResponse(
        @JsonUnwrapped KitchenTaskResponse item,
        String cancelReason,
        LocalDateTime cancelledAt,
        BigDecimal refundAmount) {
}
