package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.CounterEmployeeResponse;
import org.springboot.smartdining.dto.CounterSessionResponse;
import org.springboot.smartdining.dto.SendCodeResponse;
import org.springboot.smartdining.enums.CodeChannel;

/** Màn hình quầy: ai đang trực và vào, rời ca. */
public interface CounterService {

    /** Ca đang mở; không có thì 404 NO_OPEN_SHIFT. */
    CounterSessionResponse getCurrentShift();

    /**
     * Tìm người định vào quầy theo username (CNT-2). Chỉ trả nhân viên STAFF đang ACTIVE (CNT-6),
     * và chỉ khi chưa có ca nào mở. Ba bước vào ca chỉ làm được với username của người đang
     * đăng nhập, khác thì 403.
     */
    CounterEmployeeResponse findCandidate(String username);

    /** Phát mã 6 số và gửi qua kênh đã chọn (CNT-3, CNT-4). */
    SendCodeResponse sendCode(String username, CodeChannel channel);

    /** Nhập đúng mã thì bắt đầu ca (CNT-3); người trực được kiểm lại lúc mở (CNT-6). */
    CounterSessionResponse openShift(String username, String code);

    /** Người trực rời quầy: đóng ca đang mở của mình, ca của người khác thì 403 NOT_ON_SHIFT. */
    void closeCurrentShift();
}
