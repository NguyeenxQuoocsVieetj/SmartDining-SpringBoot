package org.springboot.smartdining.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    /** Mã ổn định trả cho client trong trường "code". */
    private final String code;

    public BadRequestException(String message) {
        this("BAD_REQUEST", message);
    }

    public BadRequestException(String code, String message) {
        super(message);
        this.code = code;
    }
}
