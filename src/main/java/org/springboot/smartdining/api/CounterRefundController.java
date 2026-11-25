package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterRefundRequest;
import org.springboot.smartdining.dto.PendingRefundResponse;
import org.springboot.smartdining.dto.RefundResponse;
import org.springboot.smartdining.service.CounterRefundService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/counter/refunds")
@RequiredArgsConstructor
public class CounterRefundController {

    private final CounterRefundService counterRefundService;

    @GetMapping("/pending")
    public List<PendingRefundResponse> getPending() {
        return counterRefundService.getPending();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse refund(@Valid @RequestBody CounterRefundRequest request) {
        return counterRefundService.refund(request.orderItemId(), request.reason());
    }
}
