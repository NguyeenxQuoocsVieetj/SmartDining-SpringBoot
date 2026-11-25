package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TableBillResponse;

/** Hóa đơn của bàn đang phục vụ. */
public interface BillService {

    /** Bàn không có: 404 NOT_FOUND; bàn không có đơn đang mở: 404 NO_OPEN_ORDER. */
    TableBillResponse getBill(Long tableId);
}
