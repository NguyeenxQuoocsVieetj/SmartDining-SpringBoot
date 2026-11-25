package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TableOrderResponse;

/**
 * Khách gọi món tại bàn. Bàn luôn lấy từ token phiên.
 * Mọi thao tác thay đổi khóa bản ghi bàn trước khi đọc đơn.
 */
public interface CustomerOrderService {

    TableOrderResponse getOrder(String token);

    /** Thêm một dòng vào giỏ; bàn chưa có đơn đang mở thì tạo đơn. */
    TableOrderResponse addItem(String token, Long productId, Integer quantity, String note);

    TableOrderResponse updateQuantity(String token, Long itemId, Integer quantity);

    /** Xóa món trong giỏ. Đơn không còn dòng nào thì xóa đơn và trả bàn về trống. */
    void removeItem(String token, Long itemId);

    /**
     * Gửi toàn bộ giỏ xuống bếp. Có một món ngừng bán thì không gửi món nào.
     * Tổng tiền đơn được tính lại từ các món, không cộng dồn.
     */
    TableOrderResponse submit(String token);
}
