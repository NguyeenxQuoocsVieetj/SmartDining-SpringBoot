package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.Category;
import org.springboot.smartdining.enums.CategoryStatus;

public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        CategoryStatus status) {

    public Category toEntity() {
        Category category = new Category();
        category.setName(name);
        category.setStatus(status);
        return category;
    }
}
