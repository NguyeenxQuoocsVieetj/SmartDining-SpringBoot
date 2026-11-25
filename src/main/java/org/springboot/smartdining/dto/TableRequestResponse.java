package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.enums.TableRequestStatus;
import org.springboot.smartdining.enums.TableRequestType;

import java.time.LocalDateTime;

public record TableRequestResponse(
        Long id,
        Long tableId,
        TableRequestType requestType,
        String serviceType,
        String note,
        TableRequestStatus status,
        LocalDateTime requestedAt,
        LocalDateTime completedAt,
        Long completedBy) {

    public static TableRequestResponse from(TableRequest request) {
        return new TableRequestResponse(request.getId(), request.getTableId(), request.getRequestType(),
                request.getServiceType(), request.getNote(), request.getStatus(), request.getRequestedAt(),
                request.getCompletedAt(), request.getCompletedBy());
    }
}
