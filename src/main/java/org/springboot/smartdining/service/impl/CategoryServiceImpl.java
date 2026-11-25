package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Category;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CategoryMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public Category create(Category input) {
        Category category = fromInput(input);
        categoryMapper.insert(category);
        return findExisting(category.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryMapper.findAll();
    }

    @Override
    public Category update(Long id, Category input) {
        findExisting(id);
        Category category = fromInput(input);
        category.setId(id);
        categoryMapper.update(category);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        findExisting(id);
        if (productMapper.countByCategoryId(id) > 0) {
            throw new ConflictException("Danh mục còn món nên không xóa được");
        }
        categoryMapper.deleteById(id);
    }

    private Category findExisting(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục " + id);
        }
        return category;
    }

    private Category fromInput(Category input) {
        require(input, "dữ liệu danh mục");
        Category category = new Category();
        category.setName(requireText(input.getName(), "name", 100));
        category.setStatus(input.getStatus());
        return category;
    }
}
