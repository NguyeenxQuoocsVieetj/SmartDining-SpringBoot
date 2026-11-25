package org.springboot.smartdining.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    /** Mã ổn định trả cho client trong trường "code". */
    private final String code;

    public ForbiddenException(String code, String message) {
        super(message);
        this.code = code;
    }
}
