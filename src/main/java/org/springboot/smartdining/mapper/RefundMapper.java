package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Refund;

import java.util.List;

@Mapper
public interface RefundMapper {

    int insert(Refund refund);

    Refund findById(Long id);

    List<Refund> findAll();

    int update(Refund refund);

    int deleteById(Long id);
}
