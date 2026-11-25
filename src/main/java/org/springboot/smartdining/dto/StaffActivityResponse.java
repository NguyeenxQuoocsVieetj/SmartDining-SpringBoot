package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

import java.math.BigDecimal;

/**
 * Hoạt động của một nhân viên trong khoảng ngày (RPT-2). Số món tính theo số phần.
 *
 * @param collected     tiền thu trong các ca người này trực
 * @param refunded      tiền hoàn trong các ca người này trực
 * @param net           thu trừ hoàn
 * @param cancellations số dòng món người này hủy
 */
public record StaffActivityResponse(
        Long employeeId,
        String fullName,
        String username,
        EmployeeRole role,
        EmployeeStatus status,
        int invoiceCount,
        BigDecimal collected,
        BigDecimal cash,
        BigDecimal transfer,
        BigDecimal refunded,
        BigDecimal net,
        int requestsHandled,
        long averageResponseSeconds,
        int itemsServed,
        int itemsCooked,
        long averageCookSeconds,
        int cancellations) {
}
