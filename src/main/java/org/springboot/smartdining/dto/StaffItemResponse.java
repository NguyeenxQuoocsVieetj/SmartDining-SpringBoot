package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderItemStatus;

import java.time.LocalDateTime;

/**
 * Một dòng món tại bàn trên màn hình phục vụ.
 * Thứ tự thành phần phải khớp resultMap trong StaffMapper.xml.
 */
public record StaffItemResponse(
        Long itemId,
        Long orderId,
        Long tableId,
        String tableName,
        String productName,
        Integer quantity,
        String note,
        OrderItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime readyAt,
        LocalDateTime servedAt) {
}
