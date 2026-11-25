package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Product;
import org.springboot.smartdining.enums.ProductStatus;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CategoryMapper;
import org.springboot.smartdining.mapper.OrderItemMapper;
import org.springboot.smartdining.mapper.ProductMapper;
import org.springboot.smartdining.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireMoney;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Product create(Product input) {
        Product product = fromInput(input);
        productMapper.insert(product);
        return findExisting(product.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return productMapper.findAll();
    }

    @Override
    public Product update(Long id, Product input) {
        findExisting(id);
        Product product = fromInput(input);
        product.setId(id);
        productMapper.update(product);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        Product existing = findExisting(id);
        if (orderItemMapper.countByProductId(id) > 0) {
            existing.setStatus(ProductStatus.DISCONTINUED);
            productMapper.update(existing);
        } else {
            productMapper.deleteById(id);
        }
    }

    private Product findExisting(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Không tìm thấy món " + id);
        }
        return product;
    }

    private Product fromInput(Product input) {
        require(input, "dữ liệu món");
        Product product = new Product();
        product.setCategoryId(requireReference(input.getCategoryId(), categoryMapper::findById, "categoryId"));
        product.setName(requireText(input.getName(), "name", 150));
        product.setImageUrl(optionalText(input.getImageUrl(), "imageUrl", 2000));
        product.setDescription(optionalText(input.getDescription(), "description", 500));
        product.setPrice(requireMoney(input.getPrice(), "price"));
        product.setStatus(input.getStatus());
        return product;
    }
}
