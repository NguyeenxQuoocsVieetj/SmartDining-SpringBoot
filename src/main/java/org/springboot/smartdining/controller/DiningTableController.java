package org.springboot.smartdining.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.DiningTableRequest;
import org.springboot.smartdining.dto.DiningTableResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.service.DiningTableService;
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
@RequestMapping("/api/v1/admin/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;

    @PostMapping
    public ResponseEntity<DiningTableResponse> create(@Valid @RequestBody DiningTableRequest request) {
        DiningTable table = diningTableService.create(request.toEntity());
        return Responses.created(table.getId(), DiningTableResponse.from(table));
    }

    @GetMapping
    public List<DiningTableResponse> getAll() {
        return diningTableService.getAll().stream().map(DiningTableResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DiningTableResponse getById(@PathVariable Long id) {
        return DiningTableResponse.from(diningTableService.getById(id));
    }

    @PutMapping("/{id}")
    public DiningTableResponse update(@PathVariable Long id, @Valid @RequestBody DiningTableRequest request) {
        return DiningTableResponse.from(diningTableService.update(id, request.toEntity()));
    }

    /** Chuyển bàn sang INACTIVE, không xóa bản ghi. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        diningTableService.delete(id);
    }
}
