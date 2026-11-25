package org.springboot.smartdining.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {

    /** Mã ổn định trả cho client trong trường "code". */
    private final String code;

    public ConflictException(String message) {
        this("CONFLICT", message);
    }

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }
}
