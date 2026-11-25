package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.EmployeeRole;

import java.time.LocalDateTime;

/**
 * Cặp token của một phiên đăng nhập.
 *
 * @param expiresIn        số giây access token còn hiệu lực
 * @param refreshExpiresAt hạn của cả phiên
 */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        LocalDateTime refreshExpiresAt,
        SignedInEmployee employee) {

    public record SignedInEmployee(
            Long id,
            String username,
            String fullName,
            EmployeeRole role) {
    }

    /** Che token để không lọt vào log. */
    @Override
    public String toString() {
        return "TokenResponse[employee=" + employee + ", refreshExpiresAt=" + refreshExpiresAt + "]";
    }
}
