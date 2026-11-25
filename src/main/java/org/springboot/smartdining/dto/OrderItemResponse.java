package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponse(
        Long id,
        Long orderId,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        String note,
        OrderItemStatus status,
        LocalDateTime createdAt,
        Long preparedBy,
        LocalDateTime startedAt,
        LocalDateTime readyAt,
        Long servedBy,
        LocalDateTime servedAt,
        Long cancelledBy,
        LocalDateTime cancelledAt,
        String cancelReason,
        Integer version) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getOrderId(), item.getProductId(), item.getQuantity(),
                item.getUnitPrice(), item.getNote(), item.getStatus(), item.getCreatedAt(), item.getPreparedBy(),
                item.getStartedAt(), item.getReadyAt(), item.getServedBy(), item.getServedAt(),
                item.getCancelledBy(), item.getCancelledAt(), item.getCancelReason(), item.getVersion());
    }
}
