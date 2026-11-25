package org.springboot.smartdining.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(min = 2, max = 150) String name,
        @Size(max = 2000) String imageUrl,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0") @Digits(integer = 11, fraction = 2) BigDecimal price,
        ProductStatus status) {

    public Product toEntity() {
        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setImageUrl(imageUrl);
        product.setDescription(description);
        product.setPrice(price);
        product.setStatus(status);
        return product;
    }
}
