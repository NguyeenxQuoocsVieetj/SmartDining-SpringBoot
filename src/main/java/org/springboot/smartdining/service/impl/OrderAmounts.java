package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/** Cách tính tiền của đơn, luôn tính lại từ các món chứ không cộng dồn. */
final class OrderAmounts {

    private OrderAmounts() {
    }

    static BigDecimal amountOf(OrderItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    /** Món được tính tiền: đã gửi bếp và không bị hủy. */
    static boolean isBilled(OrderItemStatus status) {
        return status != OrderItemStatus.CART && status != OrderItemStatus.CANCELLED;
    }

    /** Tổng tiền các món đã gửi bếp: bỏ món còn trong giỏ và món đã hủy. */
    static BigDecimal sentTotal(List<OrderItem> items) {
        return sum(items, OrderAmounts::isBilled);
    }

    static BigDecimal cartTotal(List<OrderItem> items) {
        return sum(items, status -> status == OrderItemStatus.CART);
    }

    private static BigDecimal sum(List<OrderItem> items, Predicate<OrderItemStatus> counted) {
        return items.stream()
                .filter(item -> counted.test(item.getStatus()))
                .map(OrderAmounts::amountOf)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
