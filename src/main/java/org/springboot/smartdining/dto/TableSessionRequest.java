package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;
import org.springboot.smartdining.entity.TableSession;

import java.time.LocalDateTime;

public record TableSessionRequest(
        @NotNull Long tableId,
        @NotNull LocalDateTime expiresAt,
        LocalDateTime closedAt) {

    public TableSession toEntity() {
        TableSession session = new TableSession();
        session.setTableId(tableId);
        session.setExpiresAt(expiresAt);
        session.setClosedAt(closedAt);
        return session;
    }
}
