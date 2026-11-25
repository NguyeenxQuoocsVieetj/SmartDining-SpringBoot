package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireMoney;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final CurrentUser currentUser;

    public OrderItem create(OrderItem input) {
        OrderItem item = fromInput(input);
        item.setCreatedAt(orNow(input.getCreatedAt()));
        fillByStatus(item, item.getStatus());
        orderItemMapper.insert(item);
        return findExisting(item.getId());
    }

    @Transactional(readOnly = true)
    public OrderItem getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getAll() {
        return orderItemMapper.findAll();
    }

    public OrderItem update(Long id, OrderItem input) {
        OrderItem existing = findExisting(id);
        OrderItem item = fromInput(input);
        item.setId(id);
        item.setVersion(require(input.getVersion(), "version"));
        item.setPreparedBy(existing.getPreparedBy());
        item.setStartedAt(existing.getStartedAt());
        item.setReadyAt(existing.getReadyAt());
        item.setServedBy(existing.getServedBy());
        item.setServedAt(existing.getServedAt());
        item.setCancelledBy(existing.getCancelledBy());
        item.setCancelledAt(existing.getCancelledAt());
        fillByStatus(item, item.getStatus() != null ? item.getStatus() : existing.getStatus());
        if (orderItemMapper.update(item) == 0) {
            findExisting(id);
            throw new ConflictException("Món trong đơn đã được người khác sửa, hãy tải lại");
        }
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        orderItemMapper.deleteById(id);
    }

    private OrderItem findExisting(Long id) {
        OrderItem item = orderItemMapper.findById(id);
        if (item == null) {
            throw new ResourceNotFoundException("Không tìm thấy món trong đơn " + id);
        }
        return item;
    }

    /** Điền người và giờ ứng với trạng thái, chỉ khi chưa có. */
    private void fillByStatus(OrderItem item, OrderItemStatus status) {
        if (status == null) {
            return;
        }
        switch (status) {
            case PREPARING -> {
                if (item.getPreparedBy() == null) {
                    item.setPreparedBy(currentUser.getId());
                }
                if (item.getStartedAt() == null) {
                    item.setStartedAt(now());
                }
            }
            case READY -> {
                if (item.getReadyAt() == null) {
                    item.setReadyAt(now());
                }
            }
            case SERVED -> {
                if (item.getServedBy() == null) {
                    item.setServedBy(currentUser.getId());
                }
                if (item.getServedAt() == null) {
                    item.setServedAt(now());
                }
            }
            case CANCELLED -> {
                if (item.getCancelledBy() == null) {
                    item.setCancelledBy(currentUser.getId());
                }
                if (item.getCancelledAt() == null) {
                    item.setCancelledAt(now());
                }
            }
            default -> {
                // CART, PENDING không có trường tự điền
            }
        }
    }

    /** Chỉ lấy các trường nơi gọi được phép đặt. */
    private OrderItem fromInput(OrderItem input) {
        require(input, "dữ liệu món trong đơn");
        OrderItem item = new OrderItem();
        item.setOrderId(requireReference(input.getOrderId(), orderMapper::findById, "orderId"));
        item.setProductId(requireReference(input.getProductId(), productMapper::findById, "productId"));
        int quantity = require(input.getQuantity(), "quantity");
        if (quantity < 1 || quantity > 100) {
            throw new BadRequestException("quantity phải từ 1 đến 100");
        }
        item.setQuantity(quantity);
        item.setUnitPrice(requireMoney(input.getUnitPrice(), "unitPrice"));
        item.setNote(optionalText(input.getNote(), "note", 255));
        item.setStatus(input.getStatus());
        item.setCancelReason(optionalText(input.getCancelReason(), "cancelReason", 255));
        return item;
    }
}
