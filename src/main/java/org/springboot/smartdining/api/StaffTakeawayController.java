package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TakeawayResponse;
import org.springboot.smartdining.service.TakeawayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/takeaways")
@RequiredArgsConstructor
public class StaffTakeawayController {

    private final TakeawayService takeawayService;

    @GetMapping
    public List<TakeawayResponse> getToday() {
        return takeawayService.getToday();
    }
}
