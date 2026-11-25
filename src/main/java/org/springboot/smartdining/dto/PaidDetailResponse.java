package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.OrderType;
import org.springboot.smartdining.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một khoản đã thu trong ngày. Thứ tự thành phần phải khớp resultMap trong ReportMapper.xml.
 *
 * @param tableName   tên bàn, hoặc "Mang về #mã lấy đồ"
 * @param refunded    tổng đã hoàn của khoản thu này
 * @param cashierName người trực của ca đã thu
 */
public record PaidDetailResponse(
        Long paymentId,
        Long orderId,
        OrderType orderType,
        String tableName,
        LocalDateTime paidAt,
        BigDecimal amount,
        BigDecimal refunded,
        PaymentMethod method,
        String cashierName,
        boolean hasProof) {
}
