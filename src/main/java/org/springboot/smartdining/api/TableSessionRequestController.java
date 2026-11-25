package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CustomerRequestResponse;
import org.springboot.smartdining.dto.ServiceCallRequest;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.service.CustomerRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Yêu cầu thanh toán và gọi nhân viên của khách tại bàn. */
@RestController
@RequestMapping("/api/v1/table-session")
@RequiredArgsConstructor
public class TableSessionRequestController {

    private final CustomerRequestService customerRequestService;

    /** 201 khi tạo yêu cầu mới, 200 khi trả lại yêu cầu đang chờ. */
    @PostMapping("/payment-requests")
    public ResponseEntity<CustomerRequestResponse> requestPayment(
            @RequestHeader(TableTokens.HEADER) String token) {
        CustomerRequestService.PaymentRequestResult result = customerRequestService.requestPayment(token);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.request());
    }

    @PostMapping("/service-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerRequestResponse callService(@RequestHeader(TableTokens.HEADER) String token,
                                               @Valid @RequestBody ServiceCallRequest request) {
        return customerRequestService.callService(token, request.serviceType(), request.note());
    }
}
