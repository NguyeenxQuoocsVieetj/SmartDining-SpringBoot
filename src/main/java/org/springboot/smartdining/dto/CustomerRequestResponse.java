package org.springboot.smartdining.dto;

import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.enums.TableRequestStatus;
import org.springboot.smartdining.enums.TableRequestType;

import java.time.LocalDateTime;

/** Yêu cầu của bàn như khách nhìn thấy: không có số tiền (REQ-4) và không lộ nhân viên xử lý. */
public record CustomerRequestResponse(
        Long id,
        TableRequestType requestType,
        String serviceType,
        String note,
        TableRequestStatus status,
        LocalDateTime requestedAt) {

    public static CustomerRequestResponse from(TableRequest request) {
        return new CustomerRequestResponse(request.getId(), request.getRequestType(), request.getServiceType(),
                request.getNote(), request.getStatus(), request.getRequestedAt());
    }
}
