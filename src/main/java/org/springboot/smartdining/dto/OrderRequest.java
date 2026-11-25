package org.springboot.smartdining.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** version bắt buộc khi cập nhật, bỏ qua khi tạo mới. */
public record OrderRequest(
        OrderType orderType,
        Long tableId,
        OrderStatus status,
        @DecimalMin("0") @Digits(integer = 11, fraction = 2) BigDecimal totalAmount,
        @Size(max = 255) String note,
        @Size(max = 100) String customerName,
        @Size(max = 15) String customerPhone,
        LocalDateTime pickupTime,
        Integer version) {

    public Order toEntity() {
        Order order = new Order();
        order.setOrderType(orderType);
        order.setTableId(tableId);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setNote(note);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setPickupTime(pickupTime);
        order.setVersion(version);
        return order;
    }
}
