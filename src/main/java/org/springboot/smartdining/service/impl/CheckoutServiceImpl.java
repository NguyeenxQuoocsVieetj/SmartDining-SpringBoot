package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CheckoutResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.TableRequestMapper;
import org.springboot.smartdining.mapper.TableSessionMapper;
import org.springboot.smartdining.service.CheckoutService;
import org.springboot.smartdining.storage.PaymentProofStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CheckoutServiceImpl implements CheckoutService {

    private final OpenShiftLocker openShiftLocker;
    private final DiningTableMapper diningTableMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final TableRequestMapper tableRequestMapper;
    private final TableSessionMapper tableSessionMapper;
    private final PaymentRecorder paymentRecorder;
    private final PaymentProofStorage paymentProofStorage;

    @Override
    public CheckoutResponse checkout(Long tableId, PaymentMethod method, MultipartFile proof) {
        require(method, "method");
        // Thứ tự khóa: ca, rồi bàn; sau đó mới đọc đơn
        CounterShift shift = openShiftLocker.lockOpenShift();
        DiningTable table = diningTableMapper.findByIdForUpdate(tableId);
        if (table == null) {
            throw new ResourceNotFoundException("Không tìm thấy bàn " + tableId);
        }
        Order order = orderMapper.findUnpaidByTableId(tableId);
        if (order == null) {
            throw new ConflictException("NO_OPEN_ORDER", "Bàn không có đơn đang mở");
        }
        if (tableRequestMapper.findPendingPaymentByTableId(tableId) == null) {
            throw new ConflictException("PAYMENT_NOT_REQUESTED", "Khách chưa yêu cầu thanh toán");
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        boolean unserved = items.stream().anyMatch(item ->
                OrderAmounts.isBilled(item.getStatus()) && item.getStatus() != OrderItemStatus.SERVED);
        if (unserved) {
            throw new ConflictException("ITEMS_NOT_SERVED", "Còn món chưa mang ra");
        }
        String proofPath = method == PaymentMethod.TRANSFER ? paymentProofStorage.store(proof) : null;

        orderItemMapper.deleteCartByOrderId(order.getId());
        BigDecimal total = OrderAmounts.sentTotal(items);
        order.setStatus(OrderStatus.PAID);
        order.setCashierId(shift.getEmployeeId());
        order.setTotalAmount(total);
        if (orderMapper.update(order) == 0) {
            throw new ConflictException("Đơn vừa được người khác sửa, hãy thử lại");
        }

        // Giờ lấy sau khi đã giữ khóa ca, nên không sau giờ đóng ca
        LocalDateTime paidAt = now();
        Payment payment = paymentRecorder.record(order.getId(), shift, method, total, proofPath, paidAt);
        tableRequestMapper.completePendingByTableId(tableId, shift.getEmployeeId(), paidAt);
        tableSessionMapper.closeOpenByTableId(tableId, paidAt);
        table.setStatus(TableStatus.AVAILABLE);
        if (diningTableMapper.update(table) == 0) {
            throw new ConflictException("Bàn vừa được người khác sửa, hãy thử lại");
        }
        return new CheckoutResponse(payment.getId(), order.getId(), table.getId(), table.getName(), method,
                payment.getAmount(), paidAt, shift.getId(), shift.getEmployeeId(), proofPath != null);
    }
}
