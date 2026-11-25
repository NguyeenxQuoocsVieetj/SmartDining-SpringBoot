package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.TableSession;

import java.time.LocalDateTime;

public record TableSessionResponse(
        Long id,
        Long tableId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime closedAt) {

    public static TableSessionResponse from(TableSession session) {
        return new TableSessionResponse(session.getId(), session.getTableId(), session.getCreatedAt(),
                session.getExpiresAt(), session.getClosedAt());
    }
}
