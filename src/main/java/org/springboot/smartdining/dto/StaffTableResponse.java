package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.TableStatus;

import java.math.BigDecimal;

/**
 * Một bàn trên sơ đồ của phục vụ. Các số đếm tính theo số phần (quantity), không theo số dòng.
 *
 * @param orderId       null khi bàn không có đơn đang mở
 * @param totalAmount   tiền các món đã gửi bếp, không tính món hủy
 * @param sentCount     số phần đã gửi bếp, không tính món hủy
 * @param unservedCount số phần đã gửi nhưng chưa mang ra
 * @param readyCount    số phần đã xong, chờ mang ra
 */
public record StaffTableResponse(
        Long tableId,
        String tableName,
        TableStatus tableStatus,
        Long orderId,
        BigDecimal totalAmount,
        int sentCount,
        int unservedCount,
        int readyCount) {
}
