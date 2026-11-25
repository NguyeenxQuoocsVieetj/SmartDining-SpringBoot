package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterReservationResponse;
import org.springboot.smartdining.enums.ReservationStatus;
import org.springboot.smartdining.service.CounterReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/counter/reservations")
@RequiredArgsConstructor
public class CounterReservationController {

    private final CounterReservationService counterReservationService;

    @GetMapping
    public List<CounterReservationResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String phone) {
        return counterReservationService.search(date, status, phone);
    }
}
