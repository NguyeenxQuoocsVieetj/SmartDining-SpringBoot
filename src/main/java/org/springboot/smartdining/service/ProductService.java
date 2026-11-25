package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Product;

import java.util.List;

public interface ProductService {

    Product create(Product input);

    Product getById(Long id);

    List<Product> getAll();

    Product update(Long id, Product input);

    /** Món đã có trong đơn thì chuyển DISCONTINUED, chưa có thì xóa hẳn. */
    void delete(Long id);
}
