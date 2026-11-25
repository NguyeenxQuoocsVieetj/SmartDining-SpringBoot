package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    int insert(Category category);

    Category findById(Long id);

    List<Category> findAll();

    int update(Category category);

    int deleteById(Long id);
}
