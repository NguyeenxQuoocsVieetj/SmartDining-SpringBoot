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

    CounterReservationResponse findRowById(Long id);

    /** Số lịch đã xác nhận hoặc đã đến của bàn có giờ đến nằm hẳn giữa after và before, trừ một lịch. */
    int countBookedBetween(@Param("tableId") Long tableId,
                           @Param("after") LocalDateTime after,
                           @Param("before") LocalDateTime before,
                           @Param("excludedId") Long excludedId);

    /** Chỉ xác nhận lịch còn PENDING; trả 0 nếu không còn. */
    int confirm(@Param("id") Long id,
                @Param("tableId") Long tableId,
                @Param("confirmedBy") Long confirmedBy,
                @Param("confirmedAt") LocalDateTime confirmedAt);

    /** Chỉ đổi khi trạng thái vẫn là expected; trả 0 nếu đã khác. */
    int changeStatus(@Param("id") Long id,
                     @Param("expected") ReservationStatus expected,
                     @Param("target") ReservationStatus target);
}
