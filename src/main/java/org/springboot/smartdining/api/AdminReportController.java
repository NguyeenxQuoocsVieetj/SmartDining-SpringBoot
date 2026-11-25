package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.RevenueReportResponse;
import org.springboot.smartdining.dto.StaffActivityResponse;
import org.springboot.smartdining.service.AdminReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/revenue")
    public RevenueReportResponse getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminReportService.getRevenue(from, to);
    }

    @GetMapping("/staff-activity")
    public List<StaffActivityResponse> getStaffActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminReportService.getStaffActivity(from, to);
    }
}
