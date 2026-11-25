package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.dto.CounterReservationResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.ReservationMapper;
import org.springboot.smartdining.service.CounterReservationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;

/**
 * Thao tác đổi lịch khóa theo thứ tự: ca quầy, rồi bàn (khi xếp bàn), rồi khóa đặt bàn;
 * sau đó mới đọc lịch.
 */
@Service
@Transactional(readOnly = true)
public class CounterReservationServiceImpl implements CounterReservationService {

    /** Thời điểm muộn nhất cột DATETIME so sánh được; giá trị lưu (không có phần lẻ giây) luôn nhỏ hơn. */
    private static final LocalDateTime LAST_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000);

    private final ReservationMapper reservationMapper;
    private final DiningTableMapper diningTableMapper;
    private final OpenShiftLocker openShiftLocker;
    private final ReservationWriteLock reservationWriteLock;
    /** Khoảng cách tối thiểu giữa giờ đến của hai lịch cùng bàn. */
    private final Duration tableGap;

    public CounterReservationServiceImpl(ReservationMapper reservationMapper,
                                         DiningTableMapper diningTableMapper,
                                         OpenShiftLocker openShiftLocker,
                                         ReservationWriteLock reservationWriteLock,
                                         @Value("${smartdining.reservation.table-gap:2h}") Duration tableGap) {
        if (tableGap.isNegative()) {
            throw new IllegalStateException("smartdining.reservation.table-gap không được âm");
        }
        this.reservationMapper = reservationMapper;
        this.diningTableMapper = diningTableMapper;
        this.openShiftLocker = openShiftLocker;
        this.reservationWriteLock = reservationWriteLock;
        this.tableGap = tableGap;
    }

    @Override
    public List<CounterReservationResponse> search(LocalDate date, ReservationStatus status, String phone) {
        if (date != null && (date.getYear() < 1 || date.isAfter(LAST_TIME.toLocalDate()))) {
            return List.of();
        }
        LocalDateTime from = date == null ? null : date.atStartOfDay();
        // Ngày cuối cùng database lưu được thì không có mốc trên: ngày kế tiếp đã ngoài tầm DATETIME
        LocalDateTime to = date == null || date.equals(LAST_TIME.toLocalDate())
                ? null : date.plusDays(1).atStartOfDay();
        String wantedPhone = phone == null || phone.isBlank() ? null : PhoneNumbers.normalize(phone);
        return reservationMapper.search(from, to, status, wantedPhone);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CounterReservationResponse confirm(Long id, Long tableId) {
        require(tableId, "tableId");
        CounterShift shift = openShiftLocker.lockOpenShift();
        DiningTable table = diningTableMapper.findByIdForUpdate(tableId);
        reservationWriteLock.lockUntilTransactionEnds();
        Reservation reservation = find(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw invalidTransition(reservation.getStatus(), ReservationStatus.CONFIRMED);
        }
        if (table == null) {
            throw new BadRequestException("tableId không tồn tại: " + tableId);
        }
        if (table.getStatus() == TableStatus.INACTIVE) {
            throw new ConflictException("TABLE_INACTIVE", "Bàn đã ngưng sử dụng");
        }
        requireFreeSlot(reservation, tableId);
        if (reservationMapper.confirm(id, tableId, shift.getEmployeeId(), now()) == 0) {
            throw changedByOthers();
        }
        return reservationMapper.findRowById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CounterReservationResponse arrive(Long id) {
        Reservation reservation = lockAndFind(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw invalidTransition(reservation.getStatus(), ReservationStatus.ARRIVED);
        }
        return changeStatus(reservation, ReservationStatus.ARRIVED);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CounterReservationResponse cancel(Long id) {
        Reservation reservation = lockAndFind(id);
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw invalidTransition(reservation.getStatus(), ReservationStatus.CANCELLED);
        }
        return changeStatus(reservation, ReservationStatus.CANCELLED);
    }

    private Reservation lockAndFind(Long id) {
        openShiftLocker.lockOpenShift();
        reservationWriteLock.lockUntilTransactionEnds();
        return find(id);
    }

    private CounterReservationResponse changeStatus(Reservation reservation, ReservationStatus target) {
        if (reservationMapper.changeStatus(reservation.getId(), reservation.getStatus(), target) == 0) {
            throw changedByOthers();
        }
        return reservationMapper.findRowById(reservation.getId());
    }

    /** Cách nhau đúng bằng khoảng quy định thì được. */
    private void requireFreeSlot(Reservation reservation, Long tableId) {
        LocalDateTime arrival = reservation.getArrivalTime();
        LocalDateTime before = arrival.plus(tableGap);
        int clashes = reservationMapper.countBookedBetween(tableId, arrival.minus(tableGap),
                before.isAfter(LAST_TIME) ? LAST_TIME : before, reservation.getId());
        if (clashes > 0) {
            throw new ConflictException("TABLE_ALREADY_BOOKED",
                    "Bàn đã có lịch đặt khác cách giờ này dưới " + tableGap.toMinutes() + " phút");
        }
    }

    private Reservation find(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Không tìm thấy lịch đặt " + id);
        }
        return reservation;
    }

    private static ConflictException invalidTransition(ReservationStatus from, ReservationStatus to) {
        return new ConflictException("INVALID_STATUS_TRANSITION",
                "Không chuyển được lịch đặt từ " + from + " sang " + to);
    }

    private static ConflictException changedByOthers() {
        return new ConflictException("Lịch đặt vừa được người khác sửa, hãy thử lại");
    }
}
