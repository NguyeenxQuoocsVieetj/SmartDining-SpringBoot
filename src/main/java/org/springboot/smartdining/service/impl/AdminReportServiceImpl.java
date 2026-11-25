package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.RevenueReportResponse;
import org.springboot.smartdining.dto.StaffActivityResponse;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.mapper.ReportMapper;
import org.springboot.smartdining.mapper.ReportRows;
import org.springboot.smartdining.service.AdminReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Mọi truy vấn của một báo cáo đọc cùng một thời điểm, nên các số khớp nhau. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class AdminReportServiceImpl implements AdminReportService {

    private static final BigDecimal NO_MONEY = BigDecimal.ZERO.setScale(2);

    private final ReportMapper reportMapper;

    @Override
    public RevenueReportResponse getRevenue(LocalDate from, LocalDate to) {
        ReportPeriod period = ReportPeriod.of(from, to);
        List<RevenueReportResponse.Daily> daily = reportMapper.findDailyMoney(period.from(), period.until());
        MoneyTotals totals = MoneyTotals.of(daily);
        BigDecimal average = totals.invoiceCount() == 0 ? BigDecimal.ZERO
                : totals.collected().divide(BigDecimal.valueOf(totals.invoiceCount()), 0, RoundingMode.HALF_UP);
        return new RevenueReportResponse(from, to, totals.collected(), totals.refunded(), totals.revenue(),
                totals.invoiceCount(), average, daily,
                reportMapper.findBestSellers(period.from(), period.until()));
    }

    @Override
    public List<StaffActivityResponse> getStaffActivity(LocalDate from, LocalDate to) {
        ReportPeriod period = ReportPeriod.of(from, to);
        Map<Long, ReportRows.Collected> collected = byEmployee(
                reportMapper.sumCollectedByCashier(period.from(), period.until()), ReportRows.Collected::employeeId);
        Map<Long, ReportRows.Refunded> refunded = byEmployee(
                reportMapper.sumRefundedByCashier(period.from(), period.until()), ReportRows.Refunded::employeeId);
        Map<Long, ReportRows.Requests> requests = byEmployee(
                reportMapper.sumRequestsByEmployee(period.from(), period.until()), ReportRows.Requests::employeeId);
        Map<Long, ReportRows.Served> served = byEmployee(
                reportMapper.sumServedByEmployee(period.from(), period.until()), ReportRows.Served::employeeId);
        Map<Long, ReportRows.Cooked> cooked = byEmployee(
                reportMapper.sumCookedByEmployee(period.from(), period.until()), ReportRows.Cooked::employeeId);
        Map<Long, ReportRows.Cancelled> cancelled = byEmployee(
                reportMapper.countCancelledByEmployee(period.from(), period.until()),
                ReportRows.Cancelled::employeeId);

        return reportMapper.findStaff().stream()
                .filter(staff -> staff.status() == EmployeeStatus.ACTIVE
                        || Stream.of(collected, refunded, requests, served, cooked, cancelled)
                                .anyMatch(activity -> activity.containsKey(staff.id())))
                .map(staff -> toResponse(staff, collected.get(staff.id()), refunded.get(staff.id()),
                        requests.get(staff.id()), served.get(staff.id()), cooked.get(staff.id()),
                        cancelled.get(staff.id())))
                .toList();
    }

    private static <T> Map<Long, T> byEmployee(List<T> rows, Function<T, Long> employeeId) {
        return rows.stream().collect(Collectors.toMap(employeeId, Function.identity()));
    }

    /** Tham số null nghĩa là nhân viên không có phát sinh ở mục đó. */
    private static StaffActivityResponse toResponse(ReportRows.Staff staff, ReportRows.Collected collected,
                                                    ReportRows.Refunded refunded, ReportRows.Requests requests,
                                                    ReportRows.Served served, ReportRows.Cooked cooked,
                                                    ReportRows.Cancelled cancelled) {
        BigDecimal total = collected == null ? NO_MONEY : collected.total();
        BigDecimal returned = refunded == null ? NO_MONEY : refunded.total();
        return new StaffActivityResponse(staff.id(), staff.fullName(), staff.username(), staff.role(),
                staff.status(),
                collected == null ? 0 : collected.invoiceCount(),
                total,
                collected == null ? NO_MONEY : collected.cash(),
                collected == null ? NO_MONEY : collected.transfer(),
                returned,
                total.subtract(returned),
                requests == null ? 0 : requests.handled(),
                requests == null ? 0 : average(requests.totalSeconds(), requests.handled()),
                served == null ? 0 : served.portions(),
                cooked == null ? 0 : cooked.portions(),
                cooked == null ? 0 : average(cooked.totalSeconds(), cooked.timedLines()),
                cancelled == null ? 0 : cancelled.lines());
    }

    /** Trung bình làm tròn tới giây, không có dữ liệu thì 0. */
    private static long average(long total, int count) {
        return count == 0 ? 0
                : BigDecimal.valueOf(total).divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP)
                        .longValue();
    }
}
