package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.MenuCategoryResponse;
import org.springboot.smartdining.entity.Category;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.CategoryStatus;
import org.springboot.smartdining.enums.ProductStatus;
import org.springboot.smartdining.mapper.CategoryMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<MenuCategoryResponse> getMenu() {
        Map<Long, List<MenuCategoryResponse.Item>> itemsByCategory = productMapper.findOnSale().stream()
                .collect(Collectors.groupingBy(Product::getCategoryId,
                        Collectors.mapping(MenuCategoryResponse.Item::from, Collectors.toList())));
        return categoryMapper.findAll().stream()
                .filter(category -> itemsByCategory.containsKey(category.getId()))
                .map(category -> new MenuCategoryResponse(category.getId(), category.getName(),
                        itemsByCategory.get(category.getId())))
                .toList();
    }

    @Override
    public boolean isOnSale(Product product) {
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            return false;
        }
        Category category = categoryMapper.findById(product.getCategoryId());
        return category != null && category.getStatus() == CategoryStatus.ACTIVE;
    }
}
