package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.StaffOrderResponse;
import org.springboot.smartdining.dto.StaffTableResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Màn hình của nhân viên phục vụ. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final DiningTableMapper diningTableMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    /** Sơ đồ bàn (STF-1), ẩn bàn INACTIVE. Đọc bàn, đơn, món mỗi thứ một lần. */
    public List<StaffTableResponse> getTables() {
        Map<Long, Order> orderByTable = openOrdersByTable();
        Map<Long, List<OrderItem>> itemsByOrder = openItemsByOrder();
        return diningTableMapper.findAll().stream()
                .filter(table -> table.getStatus() != TableStatus.INACTIVE)
                .map(table -> {
                    Order order = orderByTable.get(table.getId());
                    List<OrderItem> items = order == null
                            ? List.of() : itemsByOrder.getOrDefault(order.getId(), List.of());
                    return new StaffTableResponse(table.getId(), table.getName(), table.getStatus(),
                            order == null ? null : order.getId(), OrderAmounts.sentTotal(items),
                            countPortions(items, StaffService::isSent),
                            countPortions(items, status -> isSent(status) && status != OrderItemStatus.SERVED),
                            countPortions(items, status -> status == OrderItemStatus.READY));
                })
                .toList();
    }

    public StaffOrderResponse getOrder(Long tableId) {
        DiningTable table = diningTableMapper.findById(tableId);
        if (table == null) {
            throw new ResourceNotFoundException("Không tìm thấy bàn " + tableId);
        }
        Order order = orderMapper.findUnpaidByTableId(tableId);
        if (order == null) {
            throw new ResourceNotFoundException("NO_OPEN_ORDER", "Bàn không có đơn đang mở");
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        Map<Long, String> productNames = productMapper.findByOrderId(order.getId()).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));
        List<StaffOrderResponse.Item> lines = items.stream()
                .map(item -> new StaffOrderResponse.Item(item.getId(), productNames.get(item.getProductId()),
                        item.getQuantity(), item.getUnitPrice(), OrderAmounts.amountOf(item), item.getNote(),
                        item.getStatus(), item.getCancelReason(), item.getCreatedAt()))
                .toList();
        return new StaffOrderResponse(table.getId(), table.getName(), table.getStatus(), order.getId(),
                order.getCreatedAt(), OrderAmounts.sentTotal(items), lines);
    }

    private Map<Long, Order> openOrdersByTable() {
        return orderMapper.findUnpaidDineIn().stream()
                .collect(Collectors.toMap(Order::getTableId, Function.identity()));
    }

    private Map<Long, List<OrderItem>> openItemsByOrder() {
        return orderItemMapper.findOfUnpaidDineInOrders().stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private static boolean isSent(OrderItemStatus status) {
        return status != OrderItemStatus.CART && status != OrderItemStatus.CANCELLED;
    }

    private static int countPortions(List<OrderItem> items, Predicate<OrderItemStatus> counted) {
        return items.stream()
                .filter(item -> counted.test(item.getStatus()))
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
}
