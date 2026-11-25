package org.springboot.smartdining.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterShiftRequest;
import org.springboot.smartdining.dto.CounterShiftResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.service.CounterShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/counter-shifts")
@RequiredArgsConstructor
public class CounterShiftController {

    private final CounterShiftService counterShiftService;

    @PostMapping
    public ResponseEntity<CounterShiftResponse> create(@Valid @RequestBody CounterShiftRequest request) {
        CounterShift shift = counterShiftService.create(request.toEntity());
        return Responses.created(shift.getId(), CounterShiftResponse.from(shift));
    }

    @GetMapping
    public List<CounterShiftResponse> getAll() {
        return counterShiftService.getAll().stream().map(CounterShiftResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CounterShiftResponse getById(@PathVariable Long id) {
        return CounterShiftResponse.from(counterShiftService.getById(id));
    }

    @PutMapping("/{id}")
    public CounterShiftResponse update(@PathVariable Long id, @Valid @RequestBody CounterShiftRequest request) {
        return CounterShiftResponse.from(counterShiftService.update(id, request.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        counterShiftService.delete(id);
    }
}
