package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.enums.TableStatus;

public record DiningTableResponse(
        Long id,
        String name,
        String qrToken,
        TableStatus status,
        Integer version) {

    public static DiningTableResponse from(DiningTable table) {
        return new DiningTableResponse(table.getId(), table.getName(), table.getQrToken(), table.getStatus(),
                table.getVersion());
    }
}
