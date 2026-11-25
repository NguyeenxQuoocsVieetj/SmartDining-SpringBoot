package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.DeliveryState;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Một đơn mang về trên màn hình quầy, gồm mọi món ở mọi trạng thái. */
public record TakeawayResponse(
        Long orderId,
        Integer pickupCode,
        LocalDate pickupDate,
        LocalDateTime pickupTime,
        String customerName,
        String customerPhone,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        Long cashierId,
        DeliveryState deliveryState,
        List<Item> items) {

    public record Item(
            Long itemId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            String note,
            OrderItemStatus status,
            String cancelReason) {
    }
}
