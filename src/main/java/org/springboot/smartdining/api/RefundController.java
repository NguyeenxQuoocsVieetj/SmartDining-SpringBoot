package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.RefundRequest;
import org.springboot.smartdining.dto.RefundResponse;
import org.springboot.smartdining.entity.Refund;
import org.springboot.smartdining.service.RefundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponse> create(@Valid @RequestBody RefundRequest request) {
        Refund refund = refundService.create(request.toEntity());
        return Responses.created(refund.getId(), RefundResponse.from(refund));
    }

    @GetMapping
    public List<RefundResponse> getAll() {
        return refundService.getAll().stream().map(RefundResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RefundResponse getById(@PathVariable Long id) {
        return RefundResponse.from(refundService.getById(id));
    }

    @PutMapping("/{id}")
    public RefundResponse update(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return RefundResponse.from(refundService.update(id, request.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        refundService.delete(id);
    }
}
