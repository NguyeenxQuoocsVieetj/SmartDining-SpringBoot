package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.Category;
import org.springboot.smartdining.enums.CategoryStatus;

public record CategoryResponse(
        Long id,
        String name,
        CategoryStatus status) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getStatus());
    }
}
