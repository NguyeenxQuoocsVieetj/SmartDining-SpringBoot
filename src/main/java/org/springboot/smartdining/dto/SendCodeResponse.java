package org.springboot.smartdining.dto;

import org.springboot.smartdining.enums.CodeChannel;

import java.time.LocalDateTime;

/**
 * Không bao giờ chứa mã.
 *
 * @param destination email hoặc số điện thoại đã che
 */
public record SendCodeResponse(
        String username,
        CodeChannel channel,
        String destination,
        LocalDateTime expiresAt,
        long resendAfterSeconds) {
}
