package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Khóa ca đang mở trước mọi thao tác ghi của quầy, để không ghi vào ca vừa đóng.
 * Thứ tự khóa của quầy: ca, rồi bàn hoặc đơn. Transaction gọi vào dùng READ_COMMITTED.
 */
@Component
@RequiredArgsConstructor
class OpenShiftLocker {

    private final CounterShiftMapper counterShiftMapper;

    /** Ca đang mở, đã khóa tới hết transaction; không có thì 409 NO_OPEN_SHIFT. */
    @Transactional(propagation = Propagation.MANDATORY)
    CounterShift lockOpenShift() {
        CounterShift open = counterShiftMapper.findOpen();
        if (open != null) {
            // Khóa theo khóa chính, cùng thứ tự với lệnh đóng ca. Khóa bằng WHERE open_flag = 1
            // sẽ giữ index trước rồi mới tới bản ghi, ngược thứ tự và dễ deadlock với đóng ca.
            CounterShift locked = counterShiftMapper.findByIdForUpdate(open.getId());
            // Đọc lại sau khi có khóa: ca có thể vừa đóng trong lúc chờ
            if (locked != null && locked.getEndedAt() == null) {
                return locked;
            }
        }
        throw new ConflictException("NO_OPEN_SHIFT", "Chưa có ca quầy nào đang mở");
    }
}
