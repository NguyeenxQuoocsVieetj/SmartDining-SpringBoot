package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Employee;

import java.util.List;

public interface EmployeeService {

    Employee create(Employee input, String rawPassword);

    Employee getById(Long id);

    List<Employee> getAll();

    /** rawPassword null thì giữ mật khẩu đang có. */
    Employee update(Long id, Employee input, String rawPassword);

    void delete(Long id);
}
