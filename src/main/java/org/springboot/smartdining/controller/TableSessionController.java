package org.springboot.smartdining.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.TableSessionRequest;
import org.springboot.smartdining.dto.TableSessionResponse;
import org.springboot.smartdining.entity.TableSession;
import org.springboot.smartdining.service.TableSessionService;
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
@RequestMapping("/api/v1/admin/table-sessions")
@RequiredArgsConstructor
public class TableSessionController {

    private final TableSessionService tableSessionService;

    @PostMapping
    public ResponseEntity<TableSessionResponse> create(@Valid @RequestBody TableSessionRequest request) {
        TableSession session = tableSessionService.create(request.toEntity());
        return Responses.created(session.getId(), TableSessionResponse.from(session));
    }

    @GetMapping
    public List<TableSessionResponse> getAll() {
        return tableSessionService.getAll().stream().map(TableSessionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TableSessionResponse getById(@PathVariable Long id) {
        return TableSessionResponse.from(tableSessionService.getById(id));
    }

    @PutMapping("/{id}")
    public TableSessionResponse update(@PathVariable Long id, @Valid @RequestBody TableSessionRequest request) {
        return TableSessionResponse.from(tableSessionService.update(id, request.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tableSessionService.delete(id);
    }
}
