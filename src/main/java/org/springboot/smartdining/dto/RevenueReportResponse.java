package org.springboot.smartdining.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Doanh thu một khoảng ngày (RPT-1). Thu tính theo giờ thu, hoàn theo giờ hoàn; revenue = thu trừ hoàn.
 *
 * @param averageInvoice tiền thu trung bình mỗi hóa đơn, làm tròn tới đồng
 * @param daily          chỉ gồm ngày có thu hoặc hoàn, tăng dần
 */
public record RevenueReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal collected,
        BigDecimal refunded,
        BigDecimal revenue,
        int invoiceCount,
        BigDecimal averageInvoice,
        List<Daily> daily,
        List<BestSeller> bestSellers) {

    /** Thứ tự thành phần phải khớp resultMap trong ReportMapper.xml. */
    public record Daily(
            LocalDate date,
            BigDecimal collected,
            BigDecimal refunded,
            BigDecimal revenue,
            int invoiceCount) {
    }

    /** Thứ tự thành phần phải khớp resultMap trong ReportMapper.xml. */
    public record BestSeller(
            String productName,
            int quantity,
            BigDecimal revenue) {
    }
}
