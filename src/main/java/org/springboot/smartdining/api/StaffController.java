package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.StaffOrderResponse;
import org.springboot.smartdining.dto.StaffTableResponse;
import org.springboot.smartdining.service.StaffService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
