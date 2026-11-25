package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springboot.smartdining.service.ServiceChecks.optionalMoney;
import static org.springboot.smartdining.service.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.ServiceChecks.require;
import static org.springboot.smartdining.service.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderMapper orderMapper;
    private final DiningTableMapper diningTableMapper;
    private final DailyPickupCounterService dailyPickupCounterService;
    private final CurrentUser currentUser;

    public Order create(Order input) {
        require(input, "dữ liệu đơn");
        OrderType type = input.getOrderType() != null ? input.getOrderType() : OrderType.DINE_IN;
        Order order = fromInput(input, type);
        order.setCreatedAt(input.getCreatedAt());
        if (type == OrderType.TAKEAWAY) {
            LocalDate today = LocalDate.now();
            order.setPickupDate(today);
            // Cấp mã trước mọi lệnh đọc: nếu transaction đã có snapshot, MariaDB bật
            // innodb_snapshot_isolation sẽ báo lỗi khi người khác vừa tăng bộ đếm.
            order.setPickupCode(dailyPickupCounterService.nextCode(today));
            order.setCashierId(currentUser.getId());
        }
        orderMapper.insert(order);
        return findExisting(order.getId());
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return orderMapper.findAll();
    }

    /** Không đổi được loại đơn; mã lấy đồ và người thu giữ nguyên. */
    public Order update(Long id, Order input) {
        Order existing = findExisting(id);
        require(input, "dữ liệu đơn");
        OrderType type = existing.getOrderType();
        if (input.getOrderType() != null && input.getOrderType() != type) {
            throw new BadRequestException("Không được đổi loại đơn");
        }
        Order order = fromInput(input, type);
        order.setId(id);
        order.setVersion(require(input.getVersion(), "version"));
        order.setCashierId(existing.getCashierId());
        order.setPickupDate(existing.getPickupDate());
        order.setPickupCode(existing.getPickupCode());
        if (orderMapper.update(order) == 0) {
            findExisting(id);
            throw new ConflictException("Đơn đã được người khác sửa, hãy tải lại");
        }
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        orderMapper.deleteById(id);
    }

    private Order findExisting(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Không tìm thấy đơn " + id);
        }
        return order;
    }

    /** Chỉ lấy các trường nơi gọi được phép đặt. */
    private Order fromInput(Order input, OrderType type) {
        Order order = new Order();
        order.setOrderType(type);
        if (type == OrderType.DINE_IN) {
            order.setTableId(requireReference(input.getTableId(), diningTableMapper::findById, "tableId"));
        } else if (input.getTableId() != null) {
            throw new BadRequestException("Đơn mang về không được có tableId");
        }
        order.setStatus(input.getStatus());
        order.setTotalAmount(optionalMoney(input.getTotalAmount(), "totalAmount"));
        order.setNote(optionalText(input.getNote(), "note", 255));
        order.setCustomerName(optionalText(input.getCustomerName(), "customerName", 100));
        order.setCustomerPhone(optionalText(input.getCustomerPhone(), "customerPhone", 15));
        order.setPickupTime(input.getPickupTime());
        return order;
    }
}
