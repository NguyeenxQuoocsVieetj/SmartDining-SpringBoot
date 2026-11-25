package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ForbiddenException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Khóa ca đang mở trước mọi thao tác ghi của quầy, để không ghi vào ca vừa đóng, và chỉ cho
 * người đang trực ca đó làm. Thứ tự khóa của quầy: ca, rồi bàn hoặc đơn.
 * Transaction gọi vào dùng READ_COMMITTED.
 */
@Component
@RequiredArgsConstructor
class OpenShiftLocker {

    private final CounterShiftMapper counterShiftMapper;
    private final CurrentUser currentUser;

    /**
     * Ca đang mở của người đăng nhập, đã khóa tới hết transaction. Không có ca nào: 409 NO_OPEN_SHIFT;
     * ca của người khác: 403 NOT_ON_SHIFT.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    CounterShift lockOpenShift() {
        CounterShift open = counterShiftMapper.findOpen();
        if (open != null) {
            // Khóa theo khóa chính, cùng thứ tự với lệnh đóng ca. Khóa bằng WHERE open_flag = 1
            // sẽ giữ index trước rồi mới tới bản ghi, ngược thứ tự và dễ deadlock với đóng ca.
            CounterShift locked = counterShiftMapper.findByIdForUpdate(open.getId());
            // Đọc lại sau khi có khóa: ca có thể vừa đóng trong lúc chờ
            if (locked != null && locked.getEndedAt() == null) {
                requireOwner(locked);
                return locked;
            }
        }
        throw new ConflictException("NO_OPEN_SHIFT", "Chưa có ca quầy nào đang mở");
    }

    /** Dùng chung với thao tác rời ca, sau khi đã khóa bản ghi ca. */
    void requireOwner(CounterShift shift) {
        if (!shift.getEmployeeId().equals(currentUser.getId())) {
            throw new ForbiddenException("NOT_ON_SHIFT", "Chỉ người đang trực ca được thực hiện thao tác này");
        }
    }
}
