package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CheckoutResponse;
import org.springboot.smartdining.enums.PaymentMethod;
import org.springboot.smartdining.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping(path = "/api/v1/staff/tables/{id}/checkout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@PathVariable Long id,
                                     @RequestParam PaymentMethod method,
                                     @RequestPart(required = false) MultipartFile proof) {
        return checkoutService.checkout(id, method, proof);
    }
}
