package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long categoryId,
        String name,
        String imageUrl,
        String description,
        BigDecimal price,
        ProductStatus status) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getCategoryId(), product.getName(),
                product.getImageUrl(), product.getDescription(), product.getPrice(), product.getStatus());
    }
}
