package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.dto.CounterReservationResponse;
import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper {

    int insert(Reservation reservation);

    Reservation findById(Long id);

    List<Reservation> findAll();

    int update(Reservation reservation);

    int deleteById(Long id);

    int countPendingByPhone(String phone);

    /** Lịch có giờ đến trong [from, to), lọc thêm theo trạng thái và số điện thoại; null thì bỏ lọc. */
    List<CounterReservationResponse> search(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to,
                                            @Param("status") ReservationStatus status,
                                            @Param("phone") String phone);
}
