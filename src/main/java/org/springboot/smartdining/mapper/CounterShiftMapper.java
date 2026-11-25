package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.CounterShift;

import java.util.List;

@Mapper
public interface CounterShiftMapper {

    int insert(CounterShift counterShift);

    CounterShift findById(Long id);

    List<CounterShift> findAll();

    int update(CounterShift counterShift);

    int deleteById(Long id);

    /** Ca đang mở, null nếu không có. Database bảo đảm tối đa một ca. */
    CounterShift findOpen();
}
