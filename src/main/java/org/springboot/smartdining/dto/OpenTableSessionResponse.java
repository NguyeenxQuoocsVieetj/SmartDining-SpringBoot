package org.springboot.smartdining.dto;

import java.time.LocalDateTime;

/** token chỉ được trả đúng lần này. */
public record OpenTableSessionResponse(
        String token,
        Long tableId,
        String tableName,
        LocalDateTime expiresAt) {

    /** Che token để không lọt vào log. */
    @Override
    public String toString() {
        return "OpenTableSessionResponse[tableId=" + tableId + ", expiresAt=" + expiresAt + "]";
    }
}
