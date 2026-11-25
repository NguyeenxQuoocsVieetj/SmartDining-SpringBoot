package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.service.PaymentProofService;
import org.springboot.smartdining.storage.PaymentProofStorage;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPaymentProofController {

    private final PaymentProofService paymentProofService;

    @GetMapping("/api/v1/admin/payments/{id}/proof")
    public ResponseEntity<byte[]> getProof(@PathVariable Long id) {
        PaymentProofStorage.StoredImage image = paymentProofService.getProof(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(image.content());
    }
}
