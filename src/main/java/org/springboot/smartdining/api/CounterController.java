package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterEmployeeResponse;
import org.springboot.smartdining.dto.CounterSessionResponse;
import org.springboot.smartdining.service.CounterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}
