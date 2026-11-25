package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.MenuCategoryResponse;
import org.springboot.smartdining.entity.Category;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.CategoryStatus;
import org.springboot.smartdining.enums.ProductStatus;
import org.springboot.smartdining.mapper.CategoryMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Menu khách xem và quy tắc món nào đang bán. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    /** Danh mục đang bán kèm món đang bán; danh mục không còn món nào thì bỏ. */
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

    /** MNU-2: món và danh mục của nó đều phải đang bán. */
    boolean isOnSale(Product product) {
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            return false;
        }
        Category category = categoryMapper.findById(product.getCategoryId());
        return category != null && category.getStatus() == CategoryStatus.ACTIVE;
    }
}
