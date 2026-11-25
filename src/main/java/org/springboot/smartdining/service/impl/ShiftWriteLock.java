package org.springboot.smartdining.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Cho các thao tác ghi ca quầy chạy lần lượt trong ứng dụng. Nhiều lệnh cùng ghi vào unique index
 * "một ca mở" làm MariaDB deadlock. Khóa được giữ tới sau khi transaction commit hoặc rollback,
 * nên người vào sau luôn thấy kết quả của người trước.
 * Chỉ đúng khi chạy một máy chủ, giống như mã vào quầy giữ trong bộ nhớ.
 */
@Component
class ShiftWriteLock {

    private final ReentrantLock lock = new ReentrantLock();

    /** Gọi đầu tiên trong transaction, trước mọi câu lệnh đọc hay ghi ca quầy. */
    void lockUntilTransactionEnds() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Khóa ca quầy chỉ dùng được bên trong transaction");
        }
        lock.lock();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                lock.unlock();
            }
        });
    }
}
