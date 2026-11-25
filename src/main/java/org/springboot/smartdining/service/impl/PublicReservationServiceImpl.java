package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.PublicReservationRequest;
import org.springboot.smartdining.dto.PublicReservationResponse;
import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.mapper.ReservationMapper;
import org.springboot.smartdining.service.PublicReservationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class PublicReservationServiceImpl implements PublicReservationService {

    private static final Pattern PHONE = Pattern.compile("\\+?[0-9]{8,15}");
    private static final int PHONE_MAX_LENGTH = 15;
    /** Năm lớn nhất cột DATETIME lưu được. */
    private static final int MAX_YEAR = 9999;

    private final ReservationMapper reservationMapper;
    private final ReservationWriteLock reservationWriteLock;

    @Override
    public PublicReservationResponse create(PublicReservationRequest request) {
        require(request, "dữ liệu lịch đặt");
        String customerName = requireText(request.customerName(), "customerName", 100);
        String phone = requirePhone(request.phone());
        LocalDateTime createdAt = now();
        LocalDateTime arrivalTime = requireFuture(request.arrivalTime(), createdAt);

        reservationWriteLock.lockUntilTransactionEnds();
        if (reservationMapper.countPendingByPhone(phone) > 0) {
            throw pendingExists();
        }
        Reservation reservation = new Reservation();
        reservation.setCustomerName(customerName);
        reservation.setPhone(phone);
        reservation.setArrivalTime(arrivalTime);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(createdAt);
        try {
            reservationMapper.insert(reservation);
        } catch (DuplicateKeyException e) {
            // Lịch chờ vừa được tạo qua đường khác (ví dụ API quản trị)
            throw pendingExists();
        }
        return PublicReservationResponse.from(reservationMapper.findById(reservation.getId()));
    }

    private static String requirePhone(String raw) {
        String phone = PhoneNumbers.normalize(raw);
        if (phone == null || phone.length() > PHONE_MAX_LENGTH || !PHONE.matcher(phone).matches()) {
            throw new BadRequestException("Số điện thoại phải gồm 8-15 chữ số, có thể bắt đầu bằng +");
        }
        return phone;
    }

    private static LocalDateTime requireFuture(LocalDateTime arrivalTime, LocalDateTime current) {
        LocalDateTime time = require(arrivalTime, "arrivalTime").truncatedTo(ChronoUnit.SECONDS);
        if (!time.isAfter(current) || time.getYear() > MAX_YEAR) {
            throw new BadRequestException("arrivalTime phải là một thời điểm trong tương lai");
        }
        return time;
    }

    private static ConflictException pendingExists() {
        return new ConflictException("PENDING_RESERVATION_EXISTS",
                "Số điện thoại này đang có một lịch chờ xác nhận");
    }
}
