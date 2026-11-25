package org.springboot.smartdining.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    /** Mã ổn định trả cho client trong trường "code". */
    private final String code;

    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }
}
