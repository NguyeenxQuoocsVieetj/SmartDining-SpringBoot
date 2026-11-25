package org.springboot.smartdining.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;

import java.math.BigDecimal;

/** version bắt buộc khi cập nhật, bỏ qua khi tạo mới. */
public record OrderItemRequest(
        @NotNull Long orderId,
        @NotNull Long productId,
        @NotNull @Min(1) @Max(100) Integer quantity,
        @NotNull @DecimalMin("0") @Digits(integer = 11, fraction = 2) BigDecimal unitPrice,
        @Size(max = 255) String note,
        OrderItemStatus status,
        @Size(max = 255) String cancelReason,
        Integer version) {

    public OrderItem toEntity() {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setNote(note);
        item.setStatus(status);
        item.setCancelReason(cancelReason);
        item.setVersion(version);
        return item;
    }
}
