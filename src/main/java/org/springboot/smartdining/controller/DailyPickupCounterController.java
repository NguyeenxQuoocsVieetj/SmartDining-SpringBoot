package org.springboot.smartdining.controller;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.DailyPickupCounterRequest;
import org.springboot.smartdining.dto.DailyPickupCounterResponse;
import org.springboot.smartdining.dto.OnCreate;
import org.springboot.smartdining.entity.DailyPickupCounter;
import org.springboot.smartdining.service.DailyPickupCounterService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/daily-pickup-counters")
@RequiredArgsConstructor
public class DailyPickupCounterController {

    private final DailyPickupCounterService dailyPickupCounterService;

    @PostMapping
    public ResponseEntity<DailyPickupCounterResponse> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody DailyPickupCounterRequest request) {
        DailyPickupCounter counter = dailyPickupCounterService.create(request.toEntity());
        return Responses.created(counter.getBusinessDate(), DailyPickupCounterResponse.from(counter));
    }

    @GetMapping
    public List<DailyPickupCounterResponse> getAll() {
        return dailyPickupCounterService.getAll().stream().map(DailyPickupCounterResponse::from).toList();
    }

    @GetMapping("/{businessDate}")
    public DailyPickupCounterResponse getById(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        return DailyPickupCounterResponse.from(dailyPickupCounterService.getById(businessDate));
    }

    @PutMapping("/{businessDate}")
    public DailyPickupCounterResponse update(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate,
            @Valid @RequestBody DailyPickupCounterRequest request) {
        return DailyPickupCounterResponse.from(
                dailyPickupCounterService.update(businessDate, request.toEntity()));
    }

    @DeleteMapping("/{businessDate}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        dailyPickupCounterService.delete(businessDate);
    }
}
