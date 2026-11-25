package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/** Cách tính tiền của đơn, luôn tính lại từ các món chứ không cộng dồn. */
public final class OrderAmounts {

    private OrderAmounts() {
    }

    public static BigDecimal amountOf(OrderItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    /** Tổng tiền các món đã gửi bếp: bỏ món còn trong giỏ và món đã hủy. */
    public static BigDecimal sentTotal(List<OrderItem> items) {
        return sum(items, status -> status != OrderItemStatus.CART && status != OrderItemStatus.CANCELLED);
    }

    public static BigDecimal cartTotal(List<OrderItem> items) {
        return sum(items, status -> status == OrderItemStatus.CART);
    }

    private static BigDecimal sum(List<OrderItem> items, Predicate<OrderItemStatus> counted) {
        return items.stream()
                .filter(item -> counted.test(item.getStatus()))
                .map(OrderAmounts::amountOf)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
