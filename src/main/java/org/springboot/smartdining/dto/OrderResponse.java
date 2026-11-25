package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        OrderType orderType,
        Long tableId,
        Long cashierId,
        OrderStatus status,
        BigDecimal totalAmount,
        String note,
        String customerName,
        String customerPhone,
        LocalDate pickupDate,
        Integer pickupCode,
        LocalDateTime pickupTime,
        LocalDateTime createdAt,
        Integer version) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getOrderType(), order.getTableId(), order.getCashierId(),
                order.getStatus(), order.getTotalAmount(), order.getNote(), order.getCustomerName(),
                order.getCustomerPhone(), order.getPickupDate(), order.getPickupCode(), order.getPickupTime(),
                order.getCreatedAt(), order.getVersion());
    }
}
