package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TableBillResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.mapper.TableRequestMapper;
import org.springboot.smartdining.service.BillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillServiceImpl implements BillService {

    private final DiningTableMapper diningTableMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final TableRequestMapper tableRequestMapper;

    @Override
    public TableBillResponse getBill(Long tableId) {
        DiningTable table = diningTableMapper.findById(tableId);
        if (table == null) {
            throw new ResourceNotFoundException("Không tìm thấy bàn " + tableId);
        }
        Order order = orderMapper.findUnpaidByTableId(tableId);
        if (order == null) {
            throw new ResourceNotFoundException("NO_OPEN_ORDER", "Bàn không có đơn đang mở");
        }
        List<OrderItem> billed = orderItemMapper.findByOrderId(order.getId()).stream()
                .filter(item -> OrderAmounts.isBilled(item.getStatus()))
                .toList();
        Map<Long, String> productNames = productMapper.findByOrderId(order.getId()).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));
        List<TableBillResponse.Item> lines = billed.stream()
                .map(item -> new TableBillResponse.Item(item.getId(), productNames.get(item.getProductId()),
                        item.getQuantity(), item.getUnitPrice(), OrderAmounts.amountOf(item), item.getStatus()))
                .toList();

        boolean paymentRequested = tableRequestMapper.findPendingPaymentByTableId(tableId) != null;
        int unserved = billed.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.SERVED)
                .mapToInt(OrderItem::getQuantity)
                .sum();
        return new TableBillResponse(table.getId(), table.getName(), table.getStatus(), order.getId(),
                order.getCreatedAt(), lines, OrderAmounts.sentTotal(billed), paymentRequested, unserved,
                paymentRequested && unserved == 0);
    }
}
