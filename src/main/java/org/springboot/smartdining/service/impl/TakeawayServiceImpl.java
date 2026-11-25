package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TakeawayOrderRequest;
import org.springboot.smartdining.dto.TakeawayResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.DeliveryState;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.service.DailyPickupCounterService;
import org.springboot.smartdining.service.MenuService;
import org.springboot.smartdining.service.TakeawayService;
import org.springboot.smartdining.storage.PaymentProofStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TakeawayServiceImpl implements TakeawayService {

    private static final int PICKUP_GRACE_MINUTES = 5;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final OpenShiftLocker openShiftLocker;
    private final MenuService menuService;
    private final DailyPickupCounterService dailyPickupCounterService;
    private final PaymentRecorder paymentRecorder;
    private final PaymentProofStorage paymentProofStorage;

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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TakeawayResponse deliver(Long orderId) {
        CounterShift shift = openShiftLocker.lockOpenShift();
        // Xem loại đơn trước khi khóa: đơn tại bàn phải khóa bàn trước, nên không khóa ở đây
        requireTakeaway(orderMapper.findById(orderId), orderId);
        Order order = requireTakeaway(orderMapper.findByIdForUpdate(orderId), orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("ORDER_CLOSED", "Đơn đã hủy");
        }

        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        if (hasAny(items, OrderItemStatus.PENDING) || hasAny(items, OrderItemStatus.PREPARING)) {
            throw new ConflictException("ORDER_NOT_READY", "Đơn còn món chưa làm xong");
        }
        if (!hasAny(items, OrderItemStatus.READY)) {
            throw new ConflictException("ALREADY_DELIVERED", "Đơn không còn món chờ giao");
        }
        LocalDateTime servedAt = now();
        for (OrderItem item : items) {
            if (item.getStatus() == OrderItemStatus.READY) {
                item.setStatus(OrderItemStatus.SERVED);
                item.setServedBy(shift.getEmployeeId());
                item.setServedAt(servedAt);
                if (orderItemMapper.update(item) == 0) {
                    throw new ConflictException("Món vừa được người khác sửa, hãy thử lại");
                }
            }
        }
        return toResponse(order, orderItemMapper.findByOrderId(orderId),
                namesOf(productMapper.findByOrderId(orderId)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TakeawayResponse create(TakeawayOrderRequest request, MultipartFile proof) {
        require(request, "đơn mang về");
        CounterShift shift = openShiftLocker.lockOpenShift();
        Map<Long, Product> products = requireSellable(request.items());
        PaymentMethod method = require(request.method(), "method");
        String proofPath = method == PaymentMethod.TRANSFER ? paymentProofStorage.store(proof) : null;

        LocalDateTime createdAt = now();
        LocalDate today = createdAt.toLocalDate();
        List<OrderItem> items = request.items().stream()
                .map(line -> newItem(line, products.get(line.productId()), createdAt))
                .toList();
        BigDecimal total = OrderAmounts.sentTotal(items);

        Order order = new Order();
        order.setOrderType(OrderType.TAKEAWAY);
        order.setStatus(OrderStatus.PAID);
        order.setCashierId(shift.getEmployeeId());
        order.setTotalAmount(total);
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setPickupDate(today);
        // Cấp mã trong cùng transaction: đơn không tạo được thì mã cũng không bị tiêu
        order.setPickupCode(dailyPickupCounterService.nextCode(today));
        order.setPickupTime(pickupTimeOf(request.pickupTime(), createdAt));
        order.setCreatedAt(createdAt);
        orderMapper.insert(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        paymentRecorder.record(order.getId(), shift, method, total, proofPath, createdAt);
        return toResponse(orderMapper.findById(order.getId()), orderItemMapper.findByOrderId(order.getId()),
                namesOf(productMapper.findByOrderId(order.getId())));
    }

    /** Mọi món phải tồn tại (400) rồi mới xét có đang bán không (409), trước khi ghi bất cứ gì. */
    private Map<Long, Product> requireSellable(List<TakeawayOrderRequest.Item> lines) {
        Map<Long, Product> products = new LinkedHashMap<>();
        for (TakeawayOrderRequest.Item line : lines) {
            Product product = productMapper.findById(line.productId());
            if (product == null) {
                throw new BadRequestException("productId không tồn tại: " + line.productId());
            }
            products.put(product.getId(), product);
        }
        List<String> unavailable = products.values().stream()
                .filter(product -> !menuService.isOnSale(product))
                .map(Product::getName)
                .toList();
        if (!unavailable.isEmpty()) {
            throw new ConflictException("PRODUCT_UNAVAILABLE",
                    "Món hiện không bán: " + String.join(", ", unavailable));
        }
        return products;
    }

    /** Món đã trả tiền nên vào thẳng hàng đợi của bếp, đơn giá chốt lúc này. */
    private static OrderItem newItem(TakeawayOrderRequest.Item line, Product product, LocalDateTime createdAt) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setQuantity(line.quantity());
        item.setUnitPrice(product.getPrice());
        item.setNote(line.note());
        item.setStatus(OrderItemStatus.PENDING);
        item.setCreatedAt(createdAt);
        return item;
    }

    /** TKW-3: giờ lấy đã qua quá 5 phút thì hiểu là ngày mai. */
    private static LocalDateTime pickupTimeOf(String pickupTime, LocalDateTime current) {
        if (pickupTime == null) {
            return null;
        }
        LocalDateTime pickup = current.toLocalDate().atTime(LocalTime.parse(pickupTime));
        return pickup.isBefore(current.minusMinutes(PICKUP_GRACE_MINUTES)) ? pickup.plusDays(1) : pickup;
    }

    private static Order requireTakeaway(Order order, Long orderId) {
        if (order == null) {
            throw new ResourceNotFoundException("Không tìm thấy đơn " + orderId);
        }
        if (order.getOrderType() != OrderType.TAKEAWAY) {
            throw new ConflictException("INVALID_ORDER_TYPE", "Đơn tại bàn không giao ở quầy");
        }
        return order;
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
