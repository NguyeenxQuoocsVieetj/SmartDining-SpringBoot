package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.OpenTableSessionRequest;
import org.springboot.smartdining.dto.OpenTableSessionResponse;
import org.springboot.smartdining.service.CustomerSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/table-sessions")
@RequiredArgsConstructor
public class PublicTableSessionController {

    private final CustomerSessionService customerSessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpenTableSessionResponse open(@Valid @RequestBody OpenTableSessionRequest request) {
        return customerSessionService.open(request.qrToken());
    }
}
