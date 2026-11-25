package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.RegisterRequest;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.service.AccountService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AccountServiceImpl implements AccountService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RegistrationLock registrationLock;

    @Override
    public Employee register(RegisterRequest request) {
        require(request, "dữ liệu đăng ký");
        String username = requireText(request.username(), "username", 50);
        if (!USERNAME.matcher(username).matches()) {
            throw new BadRequestException("username phải dài 3-50 ký tự, chỉ gồm A-Z a-z 0-9 . _ -");
        }
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setFullName(requireText(request.fullName(), "fullName", 100));
        employee.setEmail(requireText(request.email(), "email", 100));
        employee.setPhone(optionalText(request.phone(), "phone", 15));
        employee.setRole(EmployeeRole.STAFF);
        employee.setStatus(EmployeeStatus.PENDING);
        employee.setCreatedAt(now());
        // Băm trước khi lấy khóa để các lần đăng ký không phải chờ nhau băm mật khẩu
        employee.setPasswordHash(passwordEncoder.encode(PasswordPolicy.require(request.password())));

        registrationLock.lockUntilTransactionEnds();
        if (employeeMapper.findByUsername(username) != null) {
            throw usernameTaken();
        }
        try {
            employeeMapper.insert(employee);
        } catch (DuplicateKeyException e) {
            // Tài khoản vừa được tạo qua đường khác (API quản trị)
            throw usernameTaken();
        }
        return employeeMapper.findById(employee.getId());
    }

    @Override
    public Employee approve(Long id) {
        Employee employee = employeeMapper.findByIdForUpdate(id);
        if (employee == null) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên " + id);
        }
        if (employee.getStatus() != EmployeeStatus.PENDING || employee.getRole() == EmployeeRole.ADMIN) {
            throw new ConflictException("INVALID_STATUS_TRANSITION", "Chỉ duyệt được tài khoản đang chờ duyệt");
        }
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setRole(EmployeeRole.STAFF);
        // passwordHash null: không đụng tới mật khẩu
        employee.setPasswordHash(null);
        employeeMapper.update(employee);
        return employeeMapper.findById(id);
    }

    private static ConflictException usernameTaken() {
        return new ConflictException("USERNAME_TAKEN", "Tên đăng nhập đã được dùng");
    }
}
