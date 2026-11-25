package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Khoảng ngày của báo cáo đổi sang [from, until) để lọc cột thời gian; null là không giới hạn phía đó.
 * Ngày ở rìa hoặc ngoài tầm cột DATETIME không bao giờ được gửi xuống database.
 */
record ReportPeriod(LocalDateTime from, LocalDateTime until) {

    private static final LocalDate FIRST_DAY = LocalDate.of(1, 1, 1);
    private static final LocalDate LAST_DAY = LocalDate.of(9999, 12, 31);

    /** Gồm cả hai ngày đầu cuối; from sau to thì 400. */
    static ReportPeriod of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from không được sau to");
        }
        if ((from != null && from.isAfter(LAST_DAY)) || (to != null && to.isBefore(FIRST_DAY))) {
            // Khoảng rỗng: không dòng nào vừa >= vừa < cùng một mốc
            return new ReportPeriod(FIRST_DAY.atStartOfDay(), FIRST_DAY.atStartOfDay());
        }
        LocalDateTime lower = from == null || !from.isAfter(FIRST_DAY) ? null : from.atStartOfDay();
        // Ngày cuối cùng thì không có mốc trên: ngày kế tiếp đã ngoài tầm DATETIME
        LocalDateTime upper = to == null || !to.isBefore(LAST_DAY) ? null : to.plusDays(1).atStartOfDay();
        return new ReportPeriod(lower, upper);
    }
}
