package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TakeawayResponse;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.DeliveryState;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.service.TakeawayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TakeawayServiceImpl implements TakeawayService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    /** Đọc đơn, món, tên món mỗi thứ một lần. */
    @Override
    public List<TakeawayResponse> getToday() {
        LocalDate today = LocalDate.now();
        Map<Long, List<OrderItem>> itemsByOrder = orderItemMapper.findTakeawayByPickupDate(today).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        Map<Long, String> productNames = namesOf(productMapper.findTakeawayByPickupDate(today));
        return orderMapper.findTakeawayByPickupDate(today).stream()
                .map(order -> toResponse(order, itemsByOrder.getOrDefault(order.getId(), List.of()), productNames))
                .toList();
    }

    private static Map<Long, String> namesOf(List<Product> products) {
        return products.stream().collect(Collectors.toMap(Product::getId, Product::getName));
    }

    private static TakeawayResponse toResponse(Order order, List<OrderItem> items, Map<Long, String> productNames) {
        List<TakeawayResponse.Item> lines = items.stream()
                .map(item -> new TakeawayResponse.Item(item.getId(), productNames.get(item.getProductId()),
                        item.getQuantity(), item.getUnitPrice(), item.getNote(), item.getStatus(),
                        item.getCancelReason()))
                .toList();
        return new TakeawayResponse(order.getId(), order.getPickupCode(), order.getPickupDate(),
                order.getPickupTime(), order.getCustomerName(), order.getCustomerPhone(), order.getStatus(),
                order.getTotalAmount(), order.getCreatedAt(), order.getCashierId(), deliveryState(order, items),
                lines);
    }

    /** Đơn hủy hết món là đã hủy, không được hiện thành đã giao. */
    private static DeliveryState deliveryState(Order order, List<OrderItem> items) {
        if (order.getStatus() == OrderStatus.CANCELLED
                || items.stream().allMatch(item -> item.getStatus() == OrderItemStatus.CANCELLED)) {
            return DeliveryState.CANCELLED;
        }
        if (hasAny(items, OrderItemStatus.PENDING) || hasAny(items, OrderItemStatus.PREPARING)) {
            return DeliveryState.PREPARING;
        }
        return hasAny(items, OrderItemStatus.READY) ? DeliveryState.READY : DeliveryState.DELIVERED;
    }

    private static boolean hasAny(List<OrderItem> items, OrderItemStatus status) {
        return items.stream().anyMatch(item -> item.getStatus() == status);
    }
}
