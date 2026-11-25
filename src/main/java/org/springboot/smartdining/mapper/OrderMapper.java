package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Order;

import java.util.List;

@Mapper
public interface OrderMapper {

    /** Chỉ dùng qua {@link #insert(Order)}. */
    int insertRow(Order order);

    /** Chỉ dùng qua {@link #update(Order)}. */
    int updateRow(Order order);

    default int insert(Order order) {
        int rows = insertRow(order);
        if (rows == 1 && order.getVersion() == null) {
            order.setVersion(0);
        }
        return rows;
    }

    Order findById(Long id);

    List<Order> findAll();

    /** Trả 0 khi không có id hoặc version đã cũ. */
    default int update(Order order) {
        int rows = updateRow(order);
        if (rows == 1) {
            order.setVersion(order.getVersion() + 1);
        }
        return rows;
    }

    int deleteById(Long id);
}
