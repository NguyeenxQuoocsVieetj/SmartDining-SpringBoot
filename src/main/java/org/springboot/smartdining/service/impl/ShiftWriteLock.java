package org.springboot.smartdining.service.impl;

import org.springframework.stereotype.Component;

/**
 * Cho các thao tác ghi ca quầy chạy lần lượt: nhiều lệnh cùng ghi vào unique index "một ca mở"
 * làm MariaDB deadlock. Gọi đầu tiên trong transaction.
 */
@Component
class ShiftWriteLock extends TransactionScopedLock {
}
