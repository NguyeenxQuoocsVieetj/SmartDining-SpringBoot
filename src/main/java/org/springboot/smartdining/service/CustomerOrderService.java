package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TableOrderResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.Order;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.OrderItemStatus;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.mapper.TableRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springboot.smartdining.service.ServiceChecks.now;
import static org.springboot.smartdining.service.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.ServiceChecks.require;

/**
 * Khách gọi món tại bàn. Bàn luôn lấy từ token phiên.
 * Mọi thao tác thay đổi khóa bản ghi bàn trước khi đọc đơn.
 */
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CustomerOrderService {

    private final CustomerSessionService customerSessionService;
    private final MenuService menuService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final TableRequestMapper tableRequestMapper;

    @Transactional(readOnly = true)
    public TableOrderResponse getOrder(String token) {
        return view(customerSessionService.tableOf(token));
    }

    /** Thêm một dòng vào giỏ; bàn chưa có đơn đang mở thì tạo đơn. */
    public TableOrderResponse addItem(String token, Long productId, Integer quantity, String note) {
        DiningTable table = customerSessionService.lockTable(token);

        Product product = productMapper.findById(require(productId, "productId"));
        if (product == null) {
            throw new BadRequestException("productId không tồn tại: " + productId);
        }
        requireOnSale(product);
        int validQuantity = requireQuantity(quantity);
        String cleanNote = optionalText(cleanText(note), "note", 255);

        Order order = orderMapper.findUnpaidByTableId(table.getId());
        if (order == null) {
            order = openOrder(table);
        }

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setQuantity(validQuantity);
        item.setUnitPrice(product.getPrice());
        item.setNote(cleanNote);
        item.setStatus(OrderItemStatus.CART);
        item.setCreatedAt(now());
        orderItemMapper.insert(item);
        return view(table);
    }

    public TableOrderResponse updateQuantity(String token, Long itemId, Integer quantity) {
        DiningTable table = customerSessionService.lockTable(token);
        OrderItem item = requireCartItem(orderMapper.findUnpaidByTableId(table.getId()), itemId);
        item.setQuantity(requireQuantity(quantity));
        save(item);
        return view(table);
    }

    /** Xóa món trong giỏ. Đơn không còn dòng nào thì xóa đơn và trả bàn về trống. */
    public void removeItem(String token, Long itemId) {
        DiningTable table = customerSessionService.lockTable(token);
        Order order = orderMapper.findUnpaidByTableId(table.getId());
        OrderItem item = requireCartItem(order, itemId);
        orderItemMapper.deleteById(item.getId());

        if (orderItemMapper.findByOrderId(order.getId()).isEmpty()) {
            orderMapper.deleteById(order.getId());
            // Không còn gì để thanh toán thì bỏ luôn yêu cầu thanh toán đang chờ
            tableRequestMapper.deletePendingPaymentByTableId(table.getId());
            customerSessionService.changeTableStatus(table, TableStatus.AVAILABLE);
        }
    }

    /**
     * Gửi toàn bộ giỏ xuống bếp. Có một món ngừng bán thì không gửi món nào.
     * Tổng tiền đơn được tính lại từ các món, không cộng dồn.
     */
    public TableOrderResponse submit(String token) {
        DiningTable table = customerSessionService.lockTable(token);
        Order order = orderMapper.findUnpaidByTableId(table.getId());
        List<OrderItem> items = order == null ? List.of() : orderItemMapper.findByOrderId(order.getId());
        List<OrderItem> cart = items.stream()
                .filter(item -> item.getStatus() == OrderItemStatus.CART)
                .toList();
        if (cart.isEmpty()) {
            throw new ConflictException("CART_EMPTY", "Giỏ đang trống, chưa có món để gửi bếp");
        }
        requireAllOnSale(cart);

        for (OrderItem item : cart) {
            item.setStatus(OrderItemStatus.PENDING);
            save(item);
        }
        order.setTotalAmount(OrderAmounts.sentTotal(items));
        if (orderMapper.update(order) == 0) {
            throw new ConflictException("Đơn vừa được người khác sửa, hãy thử lại");
        }
        return view(table);
    }

    private Order openOrder(DiningTable table) {
        Order order = new Order();
        order.setOrderType(OrderType.DINE_IN);
        order.setTableId(table.getId());
        order.setStatus(OrderStatus.UNPAID);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(now());
        orderMapper.insert(order);
        if (table.getStatus() == TableStatus.AVAILABLE) {
            customerSessionService.changeTableStatus(table, TableStatus.FULL);
        }
        return order;
    }

    /** Món phải thuộc đơn đang mở của bàn (không thì 404) và còn trong giỏ (không thì 409). */
    private OrderItem requireCartItem(Order order, Long itemId) {
        OrderItem item = orderItemMapper.findById(itemId);
        if (order == null || item == null || !item.getOrderId().equals(order.getId())) {
            throw new ResourceNotFoundException("Không tìm thấy món " + itemId + " trong đơn của bàn");
        }
        if (item.getStatus() != OrderItemStatus.CART) {
            throw new ConflictException("ITEM_NOT_IN_CART", "Món đã gửi bếp nên không sửa hoặc xóa được");
        }
        return item;
    }

    private void requireOnSale(Product product) {
        if (!menuService.isOnSale(product)) {
            throw new ConflictException("PRODUCT_UNAVAILABLE", "Món \"" + product.getName() + "\" hiện không bán");
        }
    }

    private void requireAllOnSale(List<OrderItem> cart) {
        List<String> unavailable = cart.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .map(productMapper::findById)
                .filter(product -> !menuService.isOnSale(product))
                .map(Product::getName)
                .toList();
        if (!unavailable.isEmpty()) {
            throw new ConflictException("PRODUCT_UNAVAILABLE",
                    "Món hiện không bán: " + String.join(", ", unavailable) + ". Hãy bỏ khỏi giỏ rồi gửi lại");
        }
    }

    private void save(OrderItem item) {
        if (orderItemMapper.update(item) == 0) {
            throw new ConflictException("Món vừa được người khác sửa, hãy thử lại");
        }
    }

    private TableOrderResponse view(DiningTable table) {
        Order order = orderMapper.findUnpaidByTableId(table.getId());
        List<OrderItem> items = order == null ? List.of() : orderItemMapper.findByOrderId(order.getId());
        Map<Long, String> productNames = order == null ? Map.of()
                : productMapper.findByOrderId(order.getId()).stream()
                        .collect(Collectors.toMap(Product::getId, Product::getName));

        List<TableOrderResponse.Item> lines = items.stream()
                .map(item -> new TableOrderResponse.Item(item.getId(), item.getProductId(),
                        productNames.get(item.getProductId()), item.getQuantity(), item.getUnitPrice(),
                        OrderAmounts.amountOf(item), item.getNote(), item.getStatus(), item.getCancelReason(),
                        item.getCreatedAt()))
                .toList();
        BigDecimal total = OrderAmounts.sentTotal(items);
        BigDecimal cart = OrderAmounts.cartTotal(items);
        return new TableOrderResponse(table.getId(), table.getName(), table.getStatus(),
                order == null ? null : order.getId(), lines, total, cart, total.add(cart));
    }

    private static int requireQuantity(Integer quantity) {
        int value = require(quantity, "quantity");
        if (value < 1 || value > 100) {
            throw new BadRequestException("quantity phải từ 1 đến 100");
        }
        return value;
    }

    /** Bỏ khoảng trắng hai đầu; chuỗi rỗng coi như không có. */
    private static String cleanText(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}
