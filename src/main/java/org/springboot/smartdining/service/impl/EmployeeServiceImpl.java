package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.service.EmployeeService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final int MIN_PASSWORD_LENGTH = 8;
    /** BCrypt chỉ nhận tối đa 72 byte. */
    private static final int MAX_PASSWORD_BYTES = 72;

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Employee create(Employee input, String rawPassword) {
        Employee employee = fromInput(input);
        employee.setPasswordHash(hash(require(rawPassword, "mật khẩu")));
        employee.setCreatedAt(orNow(input.getCreatedAt()));
        employeeMapper.insert(employee);
        return findExisting(employee.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAll() {
        return employeeMapper.findAll();
    }

    @Override
    public Employee update(Long id, Employee input, String rawPassword) {
        requireNotAdmin(findExisting(id));
        Employee employee = fromInput(input);
        employee.setId(id);
        // passwordHash null: mapper không đụng tới cột password_hash
        employee.setPasswordHash(rawPassword == null ? null : hash(rawPassword));
        employeeMapper.update(employee);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        requireNotAdmin(findExisting(id));
        employeeMapper.deleteById(id);
    }

    private Employee findExisting(Long id) {
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên " + id);
        }
        return employee;
    }

    private void requireNotAdmin(Employee employee) {
        if (employee.getRole() == EmployeeRole.ADMIN) {
            throw new ConflictException("Không được sửa hoặc xóa tài khoản ADMIN");
        }
    }

    private Employee fromInput(Employee input) {
        require(input, "dữ liệu nhân viên");
        String username = requireText(input.getUsername(), "username", 50);
        if (!USERNAME.matcher(username).matches()) {
            throw new BadRequestException("username phải dài 3-50 ký tự, chỉ gồm A-Z a-z 0-9 . _ -");
        }
        if (input.getRole() == EmployeeRole.ADMIN) {
            throw new BadRequestException("Không được tạo thêm hoặc nâng thành tài khoản ADMIN");
        }
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setFullName(requireText(input.getFullName(), "fullName", 100));
        employee.setPhone(optionalText(input.getPhone(), "phone", 15));
        employee.setEmail(requireText(input.getEmail(), "email", 100));
        employee.setAvatarUrl(optionalText(input.getAvatarUrl(), "avatarUrl", 500));
        employee.setRole(input.getRole());
        employee.setStatus(input.getStatus());
        return employee;
    }

    private String hash(String rawPassword) {
        if (rawPassword.isBlank() || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BadRequestException("Mật khẩu dài quá " + MAX_PASSWORD_BYTES + " byte");
        }
        return passwordEncoder.encode(rawPassword);
    }
}
