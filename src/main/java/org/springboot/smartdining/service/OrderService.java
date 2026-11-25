package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Order;

import java.util.List;

public interface OrderService {

    Order create(Order input);

    Order getById(Long id);

    List<Order> getAll();

    /** Không đổi được loại đơn; mã lấy đồ và người thu giữ nguyên. */
    Order update(Long id, Order input);

    void delete(Long id);
}
