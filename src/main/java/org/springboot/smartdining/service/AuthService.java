package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.MeResponse;
import org.springboot.smartdining.dto.TokenResponse;
import org.springboot.smartdining.security.EmployeePrincipal;

/** Đăng nhập và quản lý phiên đăng nhập. */
public interface AuthService {

    /**
     * Sai tên hay sai mật khẩu trả cùng một lỗi 401; mật khẩu đúng rồi mới xét trạng thái tài khoản.
     *
     * @param clientAddress địa chỉ IP của kết nối, dùng để chặn dò mật khẩu
     */
    TokenResponse login(String username, String password, String clientAddress);

    /**
     * Đổi refresh token lấy cặp mới cùng phiên, không kéo dài hạn phiên. Dùng lại token đã đổi
     * thì cả phiên bị thu hồi.
     */
    TokenResponse refresh(String refreshToken);

    void logout(EmployeePrincipal principal);

    MeResponse me(EmployeePrincipal principal);

    /** Đổi mật khẩu của chính mình rồi thu hồi mọi phiên; sai mật khẩu hiện tại cũng bị tính khi chặn dò. */
    void changePassword(EmployeePrincipal principal, String currentPassword, String newPassword,
                        String clientAddress);
}
