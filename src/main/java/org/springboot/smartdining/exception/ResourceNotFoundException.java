package org.springboot.smartdining.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    /** Mã ổn định trả cho client trong trường "code". */
    private final String code;

    public ResourceNotFoundException(String message) {
        this("NOT_FOUND", message);
    }

    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
