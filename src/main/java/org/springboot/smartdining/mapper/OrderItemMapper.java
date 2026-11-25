package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.OrderItem;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    /** Chỉ dùng qua {@link #insert(OrderItem)}. */
    int insertRow(OrderItem orderItem);

    /** Chỉ dùng qua {@link #update(OrderItem)}. */
    int updateRow(OrderItem orderItem);

    default int insert(OrderItem orderItem) {
        int rows = insertRow(orderItem);
        if (rows == 1 && orderItem.getVersion() == null) {
            orderItem.setVersion(0);
        }
        return rows;
    }

    OrderItem findById(Long id);

    List<OrderItem> findAll();

    /** Trả 0 khi không có id hoặc version đã cũ. */
    default int update(OrderItem orderItem) {
        int rows = updateRow(orderItem);
        if (rows == 1) {
            orderItem.setVersion(orderItem.getVersion() + 1);
        }
        return rows;
    }

    int deleteById(Long id);

    int countByProductId(Long productId);

    /** Mọi món của đơn, theo thứ tự thêm vào. */
    List<OrderItem> findByOrderId(Long orderId);
}
