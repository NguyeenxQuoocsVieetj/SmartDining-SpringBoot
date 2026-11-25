package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/** Một danh mục trong menu khách xem, chỉ gồm món đang bán. */
public record MenuCategoryResponse(
        Long id,
        String name,
        List<Item> products) {

    public record Item(
            Long id,
            String name,
            String imageUrl,
            String description,
            BigDecimal price) {

        public static Item from(Product product) {
            return new Item(product.getId(), product.getName(), product.getImageUrl(), product.getDescription(),
                    product.getPrice());
        }
    }
}
