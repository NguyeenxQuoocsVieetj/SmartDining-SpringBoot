package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.EmployeeDetailResponse;
import org.springboot.smartdining.dto.OverviewResponse;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.mapper.ReportMapper;
import org.springboot.smartdining.mapper.ReportRows;
import org.springboot.smartdining.service.AdminDashboardService;
import org.springboot.smartdining.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Mọi truy vấn của một lần xem đọc cùng một thời điểm, nên các số khớp nhau. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ReportMapper reportMapper;
    private final StaffService staffService;

    @Override
    public OverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        MoneyTotals money = MoneyTotals.of(
                reportMapper.findDailyMoney(today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        ReportRows.OverviewCounts counts = reportMapper.countOverview();
        return new OverviewResponse(today, money.collected(), money.refunded(), money.revenue(),
                money.invoiceCount(), counts.openTables(), counts.waitingPaymentTables(),
                counts.activeEmployees(), counts.pendingRequests());
    }

    @Override
    public List<?> getDetails(String type) {
        return switch (type == null ? "" : type) {
            case "paid" -> {
                LocalDateTime start = LocalDate.now().atStartOfDay();
                yield reportMapper.findPayments(start, start.plusDays(1));
            }
            case "open" -> reportMapper.findOpenOrders(false);
            case "waiting" -> reportMapper.findOpenOrders(true);
            case "employees" -> activeEmployees();
            case "requests" -> staffService.getPendingRequests();
            default -> throw new BadRequestException("type phải là paid, open, waiting, employees hoặc requests");
        };
    }

    private List<EmployeeDetailResponse> activeEmployees() {
        return reportMapper.findStaff().stream()
                .filter(employee -> employee.status() == EmployeeStatus.ACTIVE)
                .map(employee -> new EmployeeDetailResponse(employee.id(), employee.fullName(),
                        employee.username(), employee.role()))
                .toList();
    }
}
