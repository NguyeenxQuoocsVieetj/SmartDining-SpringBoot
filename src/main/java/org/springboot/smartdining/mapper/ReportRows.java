package org.springboot.smartdining.mapper;

import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;

import java.math.BigDecimal;

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

    /** Tiền thu trong các ca một nhân viên trực. */
    public record Collected(Long employeeId, int invoiceCount, BigDecimal total, BigDecimal cash,
                            BigDecimal transfer) {
    }

    public record Refunded(Long employeeId, BigDecimal total) {
    }

    /** @param totalSeconds tổng thời gian từ lúc khách yêu cầu tới lúc xử lý xong */
    public record Requests(Long employeeId, int handled, long totalSeconds) {
    }

    public record Served(Long employeeId, int portions) {
    }

    /** @param timedLines số dòng có đủ giờ bắt đầu và giờ xong, dùng tính trung bình */
    public record Cooked(Long employeeId, int portions, int timedLines, long totalSeconds) {
    }

    public record Cancelled(Long employeeId, int lines) {
    }
}
