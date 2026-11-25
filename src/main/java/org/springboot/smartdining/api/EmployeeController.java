package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.EmployeeRequest;
import org.springboot.smartdining.dto.EmployeeResponse;
import org.springboot.smartdining.dto.OnCreate;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.service.AccountService;
import org.springboot.smartdining.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.create(request.toEntity(), request.password());
        return Responses.created(employee.getId(), EmployeeResponse.from(employee));
    }

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll().stream().map(EmployeeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return EmployeeResponse.from(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return EmployeeResponse.from(employeeService.update(id, request.toEntity(), request.password()));
    }

    /** Duyệt tài khoản tự đăng ký (ACC-1). */
    @PostMapping("/{id}/approve")
    public EmployeeResponse approve(@PathVariable Long id) {
        return EmployeeResponse.from(accountService.approve(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
