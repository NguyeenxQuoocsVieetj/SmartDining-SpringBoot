package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TableRequestRequest;
import org.springboot.smartdining.dto.TableRequestResponse;
import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.service.TableRequestService;
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
@RequestMapping("/api/v1/admin/table-requests")
@RequiredArgsConstructor
public class TableRequestController {

    private final TableRequestService tableRequestService;

    @PostMapping
    public ResponseEntity<TableRequestResponse> create(@Valid @RequestBody TableRequestRequest request) {
        TableRequest tableRequest = tableRequestService.create(request.toEntity());
        return Responses.created(tableRequest.getId(), TableRequestResponse.from(tableRequest));
    }

    @GetMapping
    public List<TableRequestResponse> getAll() {
        return tableRequestService.getAll().stream().map(TableRequestResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TableRequestResponse getById(@PathVariable Long id) {
        return TableRequestResponse.from(tableRequestService.getById(id));
    }

    @PutMapping("/{id}")
    public TableRequestResponse update(@PathVariable Long id, @Valid @RequestBody TableRequestRequest request) {
        return TableRequestResponse.from(tableRequestService.update(id, request.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tableRequestService.delete(id);
    }
}
