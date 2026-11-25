package org.springboot.smartdining.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** So mật khẩu với mã băm, tốn thời gian như nhau dù tài khoản có tồn tại hay không. */
@Component
class PasswordChecker {

    /** BCrypt chỉ nhận tối đa 72 byte. */
    private static final int MAX_BYTES = 72;

    private final BCryptPasswordEncoder passwordEncoder;
    /** Mã băm của một chuỗi ngẫu nhiên, để so khi không có tài khoản. */
    private final String placeholderHash;

    PasswordChecker(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.placeholderHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /** @param passwordHash null khi không có tài khoản, khi đó luôn trả false */
    boolean matches(String rawPassword, String passwordHash) {
        boolean usable = rawPassword.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
        boolean matched = passwordEncoder.matches(usable ? rawPassword : "",
                passwordHash == null ? placeholderHash : passwordHash);
        return usable && passwordHash != null && matched;
    }
}
