package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Reservation;

import java.util.List;

@Mapper
public interface ReservationMapper {

    int insert(Reservation reservation);

    Reservation findById(Long id);

    List<Reservation> findAll();

    int update(Reservation reservation);

    int deleteById(Long id);
}
