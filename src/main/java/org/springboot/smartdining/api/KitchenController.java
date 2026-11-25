package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.KitchenTableResponse;
import org.springboot.smartdining.dto.KitchenTaskResponse;
import org.springboot.smartdining.service.KitchenService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
