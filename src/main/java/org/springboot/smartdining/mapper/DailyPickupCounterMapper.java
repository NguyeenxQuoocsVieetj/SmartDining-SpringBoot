package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.DailyPickupCounter;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyPickupCounterMapper {

    int insert(DailyPickupCounter dailyPickupCounter);

    DailyPickupCounter findById(LocalDate businessDate);

    List<DailyPickupCounter> findAll();

    int update(DailyPickupCounter dailyPickupCounter);

    int deleteById(LocalDate businessDate);
}
