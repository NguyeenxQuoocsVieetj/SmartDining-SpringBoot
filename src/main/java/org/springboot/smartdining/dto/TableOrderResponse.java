package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Giỏ và đơn của bàn như khách nhìn thấy.
 *
 * @param orderId         null khi bàn chưa có đơn đang mở
 * @param totalAmount     tiền các món đã gửi bếp
 * @param cartAmount      tiền các món còn trong giỏ
 * @param projectedAmount tổng nếu gửi cả giỏ
 */
public record TableOrderResponse(
        Long tableId,
        String tableName,
        TableStatus tableStatus,
        Long orderId,
        List<Item> items,
        BigDecimal totalAmount,
        BigDecimal cartAmount,
        BigDecimal projectedAmount) {

    public record Item(
            Long id,
            Long productId,
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
