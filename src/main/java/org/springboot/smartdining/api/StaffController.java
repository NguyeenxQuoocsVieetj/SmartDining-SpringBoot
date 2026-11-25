package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.StaffItemResponse;
import org.springboot.smartdining.dto.StaffOrderResponse;
import org.springboot.smartdining.dto.StaffRequestResponse;
import org.springboot.smartdining.dto.StaffTableResponse;
import org.springboot.smartdining.dto.TableRequestResponse;
import org.springboot.smartdining.service.StaffService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/tables")
    public List<StaffTableResponse> getTables() {
        return staffService.getTables();
    }

    @GetMapping("/tables/{id}/order")
    public StaffOrderResponse getOrder(@PathVariable Long id) {
        return staffService.getOrder(id);
    }

    @GetMapping("/ready-items")
    public List<StaffItemResponse> getReadyItems() {
        return staffService.getReadyItems();
    }

    @PostMapping("/items/{itemId}/serve")
    public StaffItemResponse serve(@PathVariable Long itemId) {
        return staffService.serve(itemId);
    }

    @GetMapping("/requests")
    public List<StaffRequestResponse> getRequests() {
        return staffService.getPendingRequests();
    }

    @PostMapping("/requests/{id}/complete")
    public TableRequestResponse completeRequest(@PathVariable Long id) {
        return staffService.completeRequest(id);
    }
}
