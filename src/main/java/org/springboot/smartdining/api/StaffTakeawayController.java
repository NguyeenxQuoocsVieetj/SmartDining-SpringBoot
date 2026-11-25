package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TakeawayOrderRequest;
import org.springboot.smartdining.dto.TakeawayResponse;
import org.springboot.smartdining.service.TakeawayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/takeaways")
@RequiredArgsConstructor
public class StaffTakeawayController {

    private final TakeawayService takeawayService;

    @GetMapping
    public List<TakeawayResponse> getToday() {
        return takeawayService.getToday();
    }

    /** Part "order" là JSON (application/json), part "proof" là ảnh bill khi chuyển khoản. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TakeawayResponse create(@Valid @RequestPart("order") TakeawayOrderRequest order,
                                   @RequestPart(required = false) MultipartFile proof) {
        return takeawayService.create(order, proof);
    }

    @PostMapping("/{orderId}/deliver")
    public TakeawayResponse deliver(@PathVariable Long orderId) {
        return takeawayService.deliver(orderId);
    }
}
