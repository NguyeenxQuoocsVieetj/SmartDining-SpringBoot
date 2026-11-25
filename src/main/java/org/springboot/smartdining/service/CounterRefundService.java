package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.PendingRefundResponse;
import org.springboot.smartdining.dto.RefundResponse;

import java.util.List;

/** Hoàn tiền món mang về bị hủy; quầy ghi nhận sau khi đã trả tiền cho khách. */
public interface CounterRefundService {

    /** Món chờ hoàn, hủy sớm nhất trước. */
    List<PendingRefundResponse> getPending();

    /** Ghi một khoản hoàn cho món đã hủy vào ca đang mở; mỗi món hoàn một lần. */
    RefundResponse refund(Long orderItemId, String reason);
}
