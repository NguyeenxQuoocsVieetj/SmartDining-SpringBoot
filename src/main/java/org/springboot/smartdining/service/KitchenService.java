package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.KitchenCancelResponse;
import org.springboot.smartdining.dto.KitchenTableResponse;
import org.springboot.smartdining.dto.KitchenTaskResponse;
import org.springboot.smartdining.enums.OrderItemStatus;

import java.util.List;

/** Màn hình bếp. */
public interface KitchenService {

    List<KitchenTaskResponse> getTasks();

    /** Bếp chỉ nhận làm (PENDING sang PREPARING) và báo xong (PREPARING sang READY). */
    KitchenTaskResponse changeStatus(Long itemId, OrderItemStatus target);

    /** Báo hết món (KIT-3): hủy món đang chờ hoặc đang làm và tính lại tổng tiền đơn. */
    KitchenCancelResponse cancel(Long itemId, String reason);

    /** Bàn đang sử dụng được, tức không INACTIVE. */
    List<KitchenTableResponse> getTables();
}
