package org.springboot.smartdining.service.impl;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Khóa trong ứng dụng, giữ tới sau khi transaction commit hoặc rollback, nên người vào sau luôn thấy
 * kết quả của người trước. Dùng khi nhiều lệnh cùng ghi một unique index làm MariaDB deadlock.
 * Chỉ đúng khi chạy một máy chủ.
 */
abstract class TransactionScopedLock {

    private final ReentrantLock lock = new ReentrantLock();

    /** Gọi bên trong transaction, trước khi đọc hoặc ghi dữ liệu mà khóa bảo vệ. */
    void lockUntilTransactionEnds() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Khóa này chỉ dùng được bên trong transaction");
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
