package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Một dòng món trên màn hình bếp.
 * Thứ tự thành phần phải khớp resultMap trong KitchenMapper.xml.
 *
 * @param groupName tên bàn, hoặc "Mang về #mã lấy đồ"
 */
public record KitchenTaskResponse(
        Long itemId,
        Long orderId,
        OrderType orderType,
        Long tableId,
        String groupName,
        Integer pickupCode,
        LocalDateTime pickupTime,
        Long productId,
        String productName,
        Integer quantity,
        String note,
        OrderItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime startedAt) {
}
