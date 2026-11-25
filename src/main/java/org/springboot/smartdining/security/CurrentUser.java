package org.springboot.smartdining.security;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springframework.stereotype.Component;

/**
 * Tạm thời, khi chưa bật Spring Security: người thao tác luôn là tài khoản ADMIN.
 * Chỉ dùng khi chạy trên máy cá nhân.
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final EmployeeMapper employeeMapper;

    public Long getId() {
        Long id = employeeMapper.findAdminId();
        if (id == null) {
            throw new IllegalStateException("Database chưa có tài khoản ADMIN nên không xác định được người thao tác");
        }
        return id;
    }
}
