package org.springboot.smartdining.service.impl;

import org.springframework.stereotype.Component;

/**
 * Cho việc đặt bàn, xác nhận, đến, hủy chạy lần lượt: đặt lịch mới và đổi trạng thái lịch cũ cùng ghi
 * unique index "mỗi số điện thoại một lịch chờ". Quầy phải khóa ca và bàn trước rồi mới lấy khóa này,
 * để khách đặt bàn không phải chờ theo quầy và không chờ vòng nhau.
 */
@Component
class ReservationWriteLock extends TransactionScopedLock {
}
