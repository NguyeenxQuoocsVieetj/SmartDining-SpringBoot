package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TableQrResponse;
import org.springboot.smartdining.entity.DiningTable;

/** Thao tác quản trị riêng trên bàn: kích hoạt lại và mã QR. */
public interface AdminTableService {

    /** Đưa bàn đã ngưng về trống; bàn không ngưng thì 409. */
    DiningTable activate(Long id);

    TableQrResponse getQr(Long id);

    /** Đổi mã QR và đóng mọi phiên đang mở của bàn, nên mã cũ và token cũ hết dùng. */
    TableQrResponse regenerateQr(Long id);
}
