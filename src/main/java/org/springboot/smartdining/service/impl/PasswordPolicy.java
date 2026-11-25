package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.exception.BadRequestException;

import java.nio.charset.StandardCharsets;

/** Quy tắc cho mật khẩu mới, dùng chung cho quản trị, đăng ký và đổi mật khẩu. */
final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    /** BCrypt chỉ nhận tối đa 72 byte. */
    private static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    static String require(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank() || rawPassword.length() < MIN_LENGTH) {
            throw new BadRequestException("Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BadRequestException("Mật khẩu dài quá " + MAX_BYTES + " byte");
        }
        return rawPassword;
    }
}
