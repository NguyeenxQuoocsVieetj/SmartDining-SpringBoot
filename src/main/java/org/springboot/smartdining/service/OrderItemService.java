package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.OrderItem;

import java.util.List;

public interface OrderItemService {

    OrderItem create(OrderItem input);

    OrderItem getById(Long id);

    List<OrderItem> getAll();

    OrderItem update(Long id, OrderItem input);

    void delete(Long id);
}
