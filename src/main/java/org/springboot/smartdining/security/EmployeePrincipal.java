package org.springboot.smartdining.security;

import org.springboot.smartdining.enums.EmployeeRole;

/**
 * Người đang đăng nhập của một request; vai trò đọc từ database lúc xác thực.
 *
 * @param sessionId phiên đăng nhập, dùng khi đăng xuất
 */
public record EmployeePrincipal(
        Long id,
        String username,
        EmployeeRole role,
        String sessionId) {
}
