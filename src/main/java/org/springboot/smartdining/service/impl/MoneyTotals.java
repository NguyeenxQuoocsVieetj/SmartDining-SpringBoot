package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.dto.RevenueReportResponse;

import java.math.BigDecimal;
import java.util.List;

/** Cộng các dòng thu, hoàn theo ngày, để tổng luôn khớp với bảng theo ngày. */
record MoneyTotals(BigDecimal collected, BigDecimal refunded, BigDecimal revenue, int invoiceCount) {

    static MoneyTotals of(List<RevenueReportResponse.Daily> days) {
        BigDecimal collected = BigDecimal.ZERO.setScale(2);
        BigDecimal refunded = BigDecimal.ZERO.setScale(2);
        int invoices = 0;
        for (RevenueReportResponse.Daily day : days) {
            collected = collected.add(day.collected());
            refunded = refunded.add(day.refunded());
            invoices += day.invoiceCount();
        }
        return new MoneyTotals(collected, refunded, collected.subtract(refunded), invoices);
    }
}
