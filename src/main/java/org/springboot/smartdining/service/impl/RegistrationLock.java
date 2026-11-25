package org.springboot.smartdining.service.impl;

import org.springframework.stereotype.Component;

/**
 * Cho các lần đăng ký chạy lần lượt: nhiều lệnh cùng ghi một username vào unique index có thể làm
 * MariaDB deadlock thay vì báo trùng.
 */
@Component
class RegistrationLock extends TransactionScopedLock {
}
