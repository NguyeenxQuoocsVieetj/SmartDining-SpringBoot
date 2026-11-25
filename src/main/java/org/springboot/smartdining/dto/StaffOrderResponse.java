package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Đơn đang mở của một bàn, gồm mọi món ở mọi trạng thái. */
public record StaffOrderResponse(
        Long tableId,
        String tableName,
        TableStatus tableStatus,
        Long orderId,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        List<Item> items) {

    public record Item(
            Long itemId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            String note,
            OrderItemStatus status,
            String cancelReason,
            LocalDateTime createdAt) {
    }
}
