package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.KitchenCancelResponse;
import org.springboot.smartdining.dto.KitchenTableResponse;
import org.springboot.smartdining.dto.KitchenTaskResponse;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.KitchenMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springboot.smartdining.service.KitchenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitchenServiceImpl implements KitchenService {

    private final KitchenMapper kitchenMapper;
    private final DiningTableMapper diningTableMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemLocker orderItemLocker;
    private final CurrentUser currentUser;

    @Override
    public List<KitchenTaskResponse> getTasks() {
        return kitchenMapper.findTasks();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KitchenTaskResponse changeStatus(Long itemId, OrderItemStatus target) {
        OrderItem item = orderItemLocker.lock(itemId).item();
        OrderItemStatus current = item.getStatus();
        if (current == OrderItemStatus.PENDING && target == OrderItemStatus.PREPARING) {
            item.setPreparedBy(currentUser.getId());
            item.setStartedAt(now());
        } else if (current == OrderItemStatus.PREPARING && target == OrderItemStatus.READY) {
            item.setReadyAt(now());
            if (item.getPreparedBy() == null) {
                item.setPreparedBy(currentUser.getId());
            }
        } else {
            throw OrderItemLocker.invalidTransition(current, target);
        }
        item.setStatus(target);
        save(item);
        return kitchenMapper.findTaskByItemId(itemId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KitchenCancelResponse cancel(Long itemId, String reason) {
        String cancelReason = requireText(reason == null ? null : reason.strip(), "reason", 255);
        OrderItemLocker.LockedItem locked = orderItemLocker.lock(itemId);
        Order order = locked.order();
        OrderItem item = locked.item();
        if (item.getStatus() != OrderItemStatus.PENDING && item.getStatus() != OrderItemStatus.PREPARING) {
            throw OrderItemLocker.invalidTransition(item.getStatus(), OrderItemStatus.CANCELLED);
        }

        LocalDateTime cancelledAt = now();
        item.setStatus(OrderItemStatus.CANCELLED);
        item.setCancelledBy(currentUser.getId());
        item.setCancelledAt(cancelledAt);
        item.setCancelReason(cancelReason);
        save(item);

        order.setTotalAmount(OrderAmounts.sentTotal(orderItemMapper.findByOrderId(order.getId())));
        if (orderMapper.update(order) == 0) {
            throw new ConflictException("Đơn vừa được người khác sửa, hãy thử lại");
        }

        // Đơn mang về trả tiền trước nên phải trả lại tiền món bị hủy; khoản hoàn ghi ở quầy
        boolean refundNeeded = order.getOrderType() == OrderType.TAKEAWAY && order.getStatus() == OrderStatus.PAID;
        return new KitchenCancelResponse(kitchenMapper.findTaskByItemId(itemId), cancelReason, cancelledAt,
                refundNeeded ? OrderAmounts.amountOf(item) : null);
    }

    @Override
    public List<KitchenTableResponse> getTables() {
        return diningTableMapper.findAll().stream()
                .filter(table -> table.getStatus() != TableStatus.INACTIVE)
                .map(KitchenTableResponse::from)
                .toList();
    }

    private void save(OrderItem item) {
        if (orderItemMapper.update(item) == 0) {
            throw new ConflictException("Món vừa được người khác sửa, hãy thử lại");
        }
    }
}
