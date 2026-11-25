package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.enums.TableRequestStatus;
import org.springboot.smartdining.enums.TableRequestType;

public record TableRequestRequest(
        @NotNull Long tableId,
        @NotNull TableRequestType requestType,
        @Size(max = 80) String serviceType,
        @Size(max = 255) String note,
        TableRequestStatus status) {

    public TableRequest toEntity() {
        TableRequest request = new TableRequest();
        request.setTableId(tableId);
        request.setRequestType(requestType);
        request.setServiceType(serviceType);
        request.setNote(note);
        request.setStatus(status);
        return request;
    }
}
