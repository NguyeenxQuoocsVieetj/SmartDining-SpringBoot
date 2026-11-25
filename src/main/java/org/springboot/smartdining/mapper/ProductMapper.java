package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper {

    int insert(Product product);

    Product findById(Long id);

    List<Product> findAll();

    int update(Product product);

    int deleteById(Long id);

    int countByCategoryId(Long categoryId);

    /** Món đang bán thuộc danh mục đang bán (MNU-2), xếp theo danh mục rồi theo id. */
    List<Product> findOnSale();
}
