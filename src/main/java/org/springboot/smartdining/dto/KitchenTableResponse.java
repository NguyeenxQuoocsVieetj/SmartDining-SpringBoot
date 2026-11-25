package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.enums.TableStatus;

/** Bàn như bếp nhìn thấy, không kèm mã QR. */
public record KitchenTableResponse(
        Long id,
        String name,
        TableStatus status) {

    public static KitchenTableResponse from(DiningTable table) {
        return new KitchenTableResponse(table.getId(), table.getName(), table.getStatus());
    }
}
