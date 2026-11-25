package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Món mang về đã hủy sau khi khách trả tiền, chờ quầy trả lại tiền.
 * Thứ tự thành phần phải khớp resultMap trong RefundMapper.xml.
 *
 * @param method cách khách đã trả khoản thu đó
 */
public record PendingRefundResponse(
        Long orderId,
        Integer pickupCode,
        LocalDate pickupDate,
        String customerName,
        String customerPhone,
        Long itemId,
        String productName,
        Integer quantity,
        BigDecimal amount,
        String cancelReason,
        LocalDateTime cancelledAt,
        Long paymentId,
        PaymentMethod method) {
}
