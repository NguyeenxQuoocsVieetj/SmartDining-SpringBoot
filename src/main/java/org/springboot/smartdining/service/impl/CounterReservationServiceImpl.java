package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterReservationResponse;
import org.springboot.smartdining.enums.ReservationStatus;
import org.springboot.smartdining.mapper.ReservationMapper;
import org.springboot.smartdining.service.CounterReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounterReservationServiceImpl implements CounterReservationService {

    private final ReservationMapper reservationMapper;

    @Override
    public List<CounterReservationResponse> search(LocalDate date, ReservationStatus status, String phone) {
        LocalDateTime from = date == null ? null : date.atStartOfDay();
        LocalDateTime to = date == null ? null : date.plusDays(1).atStartOfDay();
        String wantedPhone = phone == null || phone.isBlank() ? null : PhoneNumbers.normalize(phone);
        return reservationMapper.search(from, to, status, wantedPhone);
    }
}
