package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.OverviewResponse;
import org.springboot.smartdining.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/overview")
@RequiredArgsConstructor
public class AdminOverviewController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public OverviewResponse getOverview() {
        return adminDashboardService.getOverview();
    }

    @GetMapping("/details")
    public List<?> getDetails(@RequestParam(required = false) String type) {
        return adminDashboardService.getDetails(type);
    }
}
