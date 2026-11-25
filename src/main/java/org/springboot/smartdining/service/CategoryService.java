package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Category;

import java.util.List;

public interface CategoryService {

    Category create(Category input);

    Category getById(Long id);

    List<Category> getAll();

    Category update(Long id, Category input);

    void delete(Long id);
}
