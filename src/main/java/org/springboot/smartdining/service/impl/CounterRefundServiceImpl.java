package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.PendingRefundResponse;
import org.springboot.smartdining.dto.RefundResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Payment;
import org.springboot.smartdining.entity.Refund;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.PaymentMapper;
import org.springboot.smartdining.mapper.RefundMapper;
import org.springboot.smartdining.service.CounterRefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounterRefundServiceImpl implements CounterRefundService {

    private static final String DEFAULT_REASON = "Hủy món";

    private final OpenShiftLocker openShiftLocker;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;

    @Override
    public List<PendingRefundResponse> getPending() {
        return refundMapper.findPendingTakeawayRefunds();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RefundResponse refund(Long orderItemId, String reason) {
        require(orderItemId, "orderItemId");
        String cleanReason = optionalText(reason, "reason", 255);
        CounterShift shift = openShiftLocker.lockOpenShift();
        // Xem loại đơn trước khi khóa: đơn tại bàn phải khóa bàn trước, nên không khóa ở đây
        Long orderId = requireTakeaway(findItem(orderItemId).getOrderId()).getId();
        // Khóa đơn (cùng khóa với bếp) rồi đọc lại; database không chặn hoàn trùng một món
        Order order = orderMapper.findByIdForUpdate(orderId);
        OrderItem item = findItem(orderItemId);
        if (order == null || !Objects.equals(item.getOrderId(), orderId)) {
            throw new ConflictException("Món vừa được chuyển sang đơn khác, hãy thử lại");
        }
        if (item.getStatus() != OrderItemStatus.CANCELLED) {
            throw new ConflictException("ITEM_NOT_CANCELLED", "Chỉ hoàn tiền món đã hủy");
        }
        Payment payment = order.getStatus() == OrderStatus.PAID
                ? paymentMapper.findCompletedByOrderId(orderId) : null;
        if (payment == null) {
            throw new ConflictException("ORDER_NOT_PAID", "Đơn chưa thu tiền");
        }
        if (refundMapper.countByOrderItemId(orderItemId) > 0) {
            throw new ConflictException("ALREADY_REFUNDED", "Món này đã được hoàn tiền");
        }
        BigDecimal amount = OrderAmounts.amountOf(item);
        if (amount.signum() == 0) {
            throw new ConflictException("NOTHING_TO_REFUND", "Món không có tiền để hoàn");
        }
        BigDecimal refunded = refundMapper.sumByPaymentId(payment.getId());
        if (refunded.add(amount).compareTo(payment.getAmount()) > 0) {
            throw new ConflictException("REFUND_EXCEEDS_PAYMENT", "Tổng tiền hoàn vượt số đã thu");
        }

        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setOrderItemId(orderItemId);
        refund.setCounterShiftId(shift.getId());
        refund.setAmount(amount);
        refund.setReason(reasonFor(cleanReason, item));
        refund.setRefundedAt(now());
        refundMapper.insert(refund);
        return RefundResponse.from(refundMapper.findById(refund.getId()));
    }

    private OrderItem findItem(Long itemId) {
        OrderItem item = orderItemMapper.findById(itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Không tìm thấy món trong đơn " + itemId);
        }
        return item;
    }

    private Order requireTakeaway(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order.getOrderType() != OrderType.TAKEAWAY) {
            throw new ConflictException("INVALID_ORDER_TYPE", "Chỉ hoàn tiền món của đơn mang về");
        }
        return order;
    }

    private static String reasonFor(String reason, OrderItem item) {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        String cancelReason = item.getCancelReason();
        return cancelReason != null && !cancelReason.isBlank() ? cancelReason : DEFAULT_REASON;
    }
}
