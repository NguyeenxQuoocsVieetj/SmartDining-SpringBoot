package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hóa đơn tạm tính của bàn: chỉ các món đã gửi bếp và không bị hủy.
 *
 * @param paymentRequested bàn có yêu cầu thanh toán đang chờ
 * @param unservedCount    số phần trên hóa đơn chưa mang ra
 * @param canCheckout      đã đủ điều kiện thanh toán (PAY-1)
 */
public record TableBillResponse(
        Long tableId,
        String tableName,
        TableStatus tableStatus,
        Long orderId,
        LocalDateTime createdAt,
        List<Item> items,
        BigDecimal totalAmount,
        boolean paymentRequested,
        int unservedCount,
        boolean canCheckout) {

    public record Item(
            Long itemId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            OrderItemStatus status) {
    }
}
