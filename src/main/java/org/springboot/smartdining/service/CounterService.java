package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.CounterEmployeeResponse;
import org.springboot.smartdining.dto.CounterSessionResponse;

/** Màn hình quầy: ai đang trực và vào, rời ca. */
public interface CounterService {

    /** Ca đang mở; không có thì 404 NO_OPEN_SHIFT. */
    CounterSessionResponse getCurrentShift();

    /**
     * Tìm người định vào quầy theo username (CNT-2). Chỉ trả nhân viên STAFF đang ACTIVE (CNT-6),
     * và chỉ khi chưa có ca nào mở.
     */
    CounterEmployeeResponse findCandidate(String username);
}
