package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.mapper.RefreshTokenMapper;
import org.springboot.smartdining.service.EmployeeService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenMapper refreshTokenMapper;

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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Employee update(Long id, Employee input, String rawPassword) {
        // Khóa nhân viên như các thao tác phiên đăng nhập, để không sót token cấp xen vào
        Employee existing = employeeMapper.findByIdForUpdate(id);
        if (existing == null) {
            throw notFound(id);
        }
        requireNotAdmin(existing);
        Employee employee = fromInput(input);
        employee.setId(id);
        // passwordHash null: mapper không đụng tới cột password_hash
        employee.setPasswordHash(rawPassword == null ? null : hash(rawPassword));
        employeeMapper.update(employee);
        // Đổi mật khẩu hoặc cho nghỉ thì đăng xuất mọi thiết bị của người đó
        boolean disabled = input.getStatus() != null && input.getStatus() != EmployeeStatus.ACTIVE;
        if (rawPassword != null || disabled) {
            refreshTokenMapper.revokeByEmployee(id, now());
        }
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
            throw notFound(id);
        }
        return employee;
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Không tìm thấy nhân viên " + id);
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
        return passwordEncoder.encode(PasswordPolicy.require(rawPassword));
    }
}
