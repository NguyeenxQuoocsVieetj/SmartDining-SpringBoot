package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một đơn tại bàn chưa trả. Các số đếm tính theo số phần.
 * Thứ tự thành phần phải khớp resultMap trong ReportMapper.xml.
 *
 * @param totalAmount tiền các món đã gửi bếp, không tính món hủy
 * @param sentCount   số phần đã gửi bếp, không tính món hủy
 */
public record OpenOrderDetailResponse(
        Long orderId,
        Long tableId,
        String tableName,
        TableStatus tableStatus,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        int sentCount,
        int servedCount,
        int cartCount) {
}
