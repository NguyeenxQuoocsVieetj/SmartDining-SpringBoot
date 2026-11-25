package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.MenuCategoryResponse;
import org.springboot.smartdining.entity.Product;

import java.util.List;

/** Menu khách xem và quy tắc món nào đang bán. */
public interface MenuService {

    /** Danh mục đang bán kèm món đang bán; danh mục không còn món nào thì bỏ. */
    List<MenuCategoryResponse> getMenu();

    /** MNU-2: món và danh mục của nó đều phải đang bán. */
    boolean isOnSale(Product product);
}
