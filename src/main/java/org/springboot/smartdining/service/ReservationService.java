package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Reservation;
import org.springboot.smartdining.enums.ReservationStatus;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.ReservationMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.ServiceChecks.now;
import static org.springboot.smartdining.service.ServiceChecks.optionalReference;
import static org.springboot.smartdining.service.ServiceChecks.require;
import static org.springboot.smartdining.service.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationMapper reservationMapper;
    private final DiningTableMapper diningTableMapper;
    private final CurrentUser currentUser;

    public Reservation create(Reservation input) {
        Reservation reservation = fromInput(input);
        reservation.setCreatedAt(input.getCreatedAt());
        fillConfirmation(reservation, reservation.getStatus());
        reservationMapper.insert(reservation);
        return findExisting(reservation.getId());
    }

    @Transactional(readOnly = true)
    public Reservation getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAll() {
        return reservationMapper.findAll();
    }

    public Reservation update(Long id, Reservation input) {
        Reservation existing = findExisting(id);
        Reservation reservation = fromInput(input);
        reservation.setId(id);
        reservation.setConfirmedBy(existing.getConfirmedBy());
        reservation.setConfirmedAt(existing.getConfirmedAt());
        ReservationStatus status = reservation.getStatus() != null ? reservation.getStatus() : existing.getStatus();
        fillConfirmation(reservation, status);
        reservationMapper.update(reservation);
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        reservationMapper.deleteById(id);
    }

    private Reservation findExisting(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Không tìm thấy lịch đặt " + id);
        }
        return reservation;
    }

    /** Lịch đã xác nhận hoặc khách đã đến thì phải có bàn, người và giờ xác nhận. */
    private void fillConfirmation(Reservation reservation, ReservationStatus status) {
        if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.ARRIVED) {
            return;
        }
        require(reservation.getTableId(), "tableId cho lịch đã xác nhận");
        if (reservation.getConfirmedBy() == null) {
            reservation.setConfirmedBy(currentUser.getId());
        }
        if (reservation.getConfirmedAt() == null) {
            reservation.setConfirmedAt(now());
        }
    }

    private Reservation fromInput(Reservation input) {
        require(input, "dữ liệu lịch đặt");
        Reservation reservation = new Reservation();
        reservation.setCustomerName(requireText(input.getCustomerName(), "customerName", 100));
        reservation.setPhone(requireText(normalizePhone(input.getPhone()), "phone", 15));
        reservation.setArrivalTime(require(input.getArrivalTime(), "arrivalTime"));
        reservation.setTableId(optionalReference(input.getTableId(), diningTableMapper::findById, "tableId"));
        reservation.setStatus(input.getStatus());
        return reservation;
    }

    /** Bỏ khoảng trắng, dấu chấm, gạch; đổi đầu +84 thành 0. */
    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[\\s\\u00A0.\\-]", "");
        return digits.startsWith("+84") ? "0" + digits.substring(3) : digits;
    }
}
