package org.springboot.smartdining.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tổng quan hôm nay (ADM-1). revenue là thực thu: thu trừ hoàn.
 *
 * @param openTables           số đơn tại bàn chưa trả
 * @param waitingPaymentTables số bàn đang chờ thanh toán
 */
public record OverviewResponse(
        LocalDate date,
        BigDecimal collected,
        BigDecimal refunded,
        BigDecimal revenue,
        int invoiceCount,
        int openTables,
        int waitingPaymentTables,
        int activeEmployees,
        int pendingRequests) {
}
