package org.springboot.smartdining.api;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.DiningTableResponse;
import org.springboot.smartdining.dto.TableQrResponse;
import org.springboot.smartdining.service.AdminTableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tables/{id}")
@RequiredArgsConstructor
public class AdminTableController {

    private final AdminTableService adminTableService;

    @PostMapping("/activate")
    public DiningTableResponse activate(@PathVariable Long id) {
        return DiningTableResponse.from(adminTableService.activate(id));
    }

    @GetMapping("/qr")
    public TableQrResponse getQr(@PathVariable Long id) {
        return adminTableService.getQr(id);
    }

    @PostMapping("/qr/regenerate")
    public TableQrResponse regenerateQr(@PathVariable Long id) {
        return adminTableService.regenerateQr(id);
    }
}
