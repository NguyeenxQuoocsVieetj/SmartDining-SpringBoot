package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.PublicReservationRequest;
import org.springboot.smartdining.dto.PublicReservationResponse;
import org.springboot.smartdining.service.PublicReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/reservations")
@RequiredArgsConstructor
public class PublicReservationController {

    private final PublicReservationService publicReservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicReservationResponse create(@Valid @RequestBody PublicReservationRequest request) {
        return publicReservationService.create(request);
    }
}
