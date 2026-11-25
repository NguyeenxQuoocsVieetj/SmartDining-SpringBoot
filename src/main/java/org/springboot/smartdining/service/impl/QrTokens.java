package org.springboot.smartdining.service.impl;

import java.security.SecureRandom;
import java.util.HexFormat;

/** Mã ngẫu nhiên in trong QR của bàn. */
final class QrTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private QrTokens() {
    }

    /** 16 byte ngẫu nhiên viết dạng hex, đủ 32 ký tự. */
    static String newToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
