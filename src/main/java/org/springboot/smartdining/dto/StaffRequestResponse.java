package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.TableRequestType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yêu cầu đang chờ trên màn hình phục vụ và quầy.
 * Thứ tự thành phần phải khớp resultMap trong StaffMapper.xml.
 *
 * @param amountDue chỉ có với yêu cầu thanh toán: tiền các món đã gửi của đơn đang mở (REQ-4)
 */
public record StaffRequestResponse(
        Long id,
        Long tableId,
        String tableName,
        TableRequestType requestType,
        String serviceType,
        String note,
        LocalDateTime requestedAt,
        BigDecimal amountDue) {

    public StaffRequestResponse withAmountDue(BigDecimal amount) {
        return new StaffRequestResponse(id, tableId, tableName, requestType, serviceType, note, requestedAt, amount);
    }
}
