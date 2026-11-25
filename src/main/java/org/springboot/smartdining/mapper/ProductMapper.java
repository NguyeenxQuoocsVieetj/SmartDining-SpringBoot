package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Product;

import java.time.LocalDate;
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

    /** Các món xuất hiện trong một đơn, mỗi món một lần. */
    List<Product> findByOrderId(Long orderId);

    /** Các món xuất hiện trong đơn mang về lấy trong một ngày, mỗi món một lần. */
    List<Product> findTakeawayByPickupDate(LocalDate pickupDate);
}
