package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Khóa trước khi nhân viên đổi một món. Thứ tự khóa giống phía khách (bàn, rồi đơn, rồi món)
 * để hai bên không chờ vòng nhau. Đơn không có bàn thì khóa bản ghi đơn.
 * Transaction gọi vào dùng READ_COMMITTED để đọc được dữ liệu mới nhất sau khi chờ khóa.
 */
@Component
@RequiredArgsConstructor
class OrderItemLocker {

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final DiningTableMapper diningTableMapper;

    record LockedItem(Order order, OrderItem item) {
    }

    /** Món cùng đơn của nó, đọc sau khi đã khóa. Đơn đã đóng thì từ chối. */
    @Transactional(propagation = Propagation.MANDATORY)
    LockedItem lock(Long itemId) {
        OrderItem found = findItem(itemId);
        Order owner = orderMapper.findById(found.getOrderId());
        if (owner.getTableId() != null) {
            diningTableMapper.findByIdForUpdate(owner.getTableId());
        } else {
            orderMapper.findByIdForUpdate(owner.getId());
        }

        // Đọc lại: trong lúc chờ khóa, người khác có thể đã đổi món hoặc đơn
        OrderItem item = findItem(itemId);
        Order order = orderMapper.findById(item.getOrderId());
        if (!Objects.equals(item.getOrderId(), owner.getId())
                || !Objects.equals(order.getTableId(), owner.getTableId())) {
            throw new ConflictException("Món vừa được chuyển sang đơn hoặc bàn khác, hãy thử lại");
        }
        requireOpen(order);
        return new LockedItem(order, item);
    }

    static ConflictException invalidTransition(OrderItemStatus from, OrderItemStatus to) {
        return new ConflictException("INVALID_STATUS_TRANSITION",
                "Không chuyển được món từ " + from + " sang " + to);
    }

    private OrderItem findItem(Long itemId) {
        OrderItem item = orderItemMapper.findById(itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Không tìm thấy món trong đơn " + itemId);
        }
        return item;
    }

    private void requireOpen(Order order) {
        boolean dineInClosed = order.getOrderType() == OrderType.DINE_IN && order.getStatus() != OrderStatus.UNPAID;
        if (dineInClosed || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("ORDER_CLOSED", "Đơn đã đóng nên không đổi món được");
        }
    }
}
