package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterEmployeeResponse;
import org.springboot.smartdining.dto.CounterSessionResponse;
import org.springboot.smartdining.dto.OpenShiftRequest;
import org.springboot.smartdining.dto.SendCodeRequest;
import org.springboot.smartdining.dto.SendCodeResponse;
import org.springboot.smartdining.service.CounterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counter")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @GetMapping("/shift")
    public CounterSessionResponse getCurrentShift() {
        return counterService.getCurrentShift();
    }

    @GetMapping("/employees")
    public CounterEmployeeResponse findCandidate(@RequestParam String username) {
        return counterService.findCandidate(username);
    }

    @PostMapping("/shift")
    @ResponseStatus(HttpStatus.CREATED)
    public CounterSessionResponse openShift(@Valid @RequestBody OpenShiftRequest request) {
        return counterService.openShift(request.username(), request.code());
    }

    @DeleteMapping("/shift")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeCurrentShift() {
        counterService.closeCurrentShift();
    }

    @PostMapping("/shift/codes")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SendCodeResponse sendCode(@Valid @RequestBody SendCodeRequest request) {
        return counterService.sendCode(request.username(), request.channel());
    }
}
