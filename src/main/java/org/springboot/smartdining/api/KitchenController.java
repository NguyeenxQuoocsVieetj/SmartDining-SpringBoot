package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.KitchenCancelRequest;
import org.springboot.smartdining.dto.KitchenCancelResponse;
import org.springboot.smartdining.dto.KitchenStatusRequest;
import org.springboot.smartdining.dto.KitchenTableResponse;
import org.springboot.smartdining.dto.KitchenTaskResponse;
import org.springboot.smartdining.service.KitchenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/tasks")
    public List<KitchenTaskResponse> getTasks() {
        return kitchenService.getTasks();
    }

    @GetMapping("/tables")
    public List<KitchenTableResponse> getTables() {
        return kitchenService.getTables();
    }

    @PatchMapping("/items/{itemId}/status")
    public KitchenTaskResponse changeStatus(@PathVariable Long itemId,
                                            @Valid @RequestBody KitchenStatusRequest request) {
        return kitchenService.changeStatus(itemId, request.status());
    }

    @PostMapping("/items/{itemId}/cancel")
    public KitchenCancelResponse cancel(@PathVariable Long itemId,
                                        @Valid @RequestBody KitchenCancelRequest request) {
        return kitchenService.cancel(itemId, request.reason());
    }
}
