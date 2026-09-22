package com.ipagency.common;

import org.springframework.http.HttpStatus;

public final class BusinessCodes {
    private BusinessCodes() {}
    public static BusinessException conflict(String code, String message) {
        return new BusinessException(code + ": " + message, HttpStatus.CONFLICT);
    }
    public static BusinessException bad(String code, String message) {
        return new BusinessException(code + ": " + message);
    }
    public static BusinessException forbidden(String code, String message) {
        return new BusinessException(code + ": " + message, HttpStatus.FORBIDDEN);
    }
}
