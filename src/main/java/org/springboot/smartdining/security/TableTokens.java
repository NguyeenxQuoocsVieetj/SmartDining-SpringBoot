package org.springboot.smartdining.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Token phiên bàn: client giữ chuỗi gốc, database chỉ giữ SHA-256 của nó. */
public final class TableTokens {

    /** Header khách gửi token lên. */
    public static final String HEADER = "X-Table-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private TableTokens() {
    }

    /** 32 byte ngẫu nhiên viết dạng hex, 64 ký tự. */
    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** SHA-256 của token, 64 ký tự hex, đúng cột token_hash. */
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Máy ảo Java không hỗ trợ SHA-256", e);
        }
    }
}
