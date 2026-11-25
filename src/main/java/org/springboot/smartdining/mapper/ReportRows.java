package org.springboot.smartdining.mapper;

import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

/**
 * Dòng kết quả trung gian của các truy vấn báo cáo.
 * Thứ tự thành phần phải khớp resultMap trong ReportMapper.xml.
 */
public final class ReportRows {

    private ReportRows() {
    }

    public record OverviewCounts(int openTables, int waitingPaymentTables, int activeEmployees,
                                 int pendingRequests) {
    }

    /** Nhân viên, không kèm mật khẩu băm. */
    public record Staff(Long id, String fullName, String username, EmployeeRole role, EmployeeStatus status) {
    }
}
