package org.springboot.smartdining.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Người đang đăng nhập của request hiện tại. */
@Component
public class CurrentUser {

    /** Chỉ gọi trong request đã xác thực; không có người đăng nhập là lỗi cấu hình phân quyền. */
    public EmployeePrincipal require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EmployeePrincipal principal) {
            return principal;
        }
        throw new IllegalStateException("Thao tác này cần người đã đăng nhập");
    }

    public Long getId() {
        return require().id();
    }
}
