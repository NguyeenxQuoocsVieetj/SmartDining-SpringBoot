package org.springboot.smartdining.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.OrderItemRequest;
import org.springboot.smartdining.dto.OrderItemResponse;
import org.springboot.smartdining.entity.OrderItem;
import org.springboot.smartdining.service.OrderItemService;
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
@RequestMapping("/api/v1/admin/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponse> create(@Valid @RequestBody OrderItemRequest request) {
        OrderItem item = orderItemService.create(request.toEntity());
        return Responses.created(item.getId(), OrderItemResponse.from(item));
    }

    @GetMapping
    public List<OrderItemResponse> getAll() {
        return orderItemService.getAll().stream().map(OrderItemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderItemResponse getById(@PathVariable Long id) {
        return OrderItemResponse.from(orderItemService.getById(id));
    }

    @PutMapping("/{id}")
    public OrderItemResponse update(@PathVariable Long id, @Valid @RequestBody OrderItemRequest request) {
        return OrderItemResponse.from(orderItemService.update(id, request.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderItemService.delete(id);
    }
}
