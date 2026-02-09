package com.nonggle.server.common;

import org.springframework.http.HttpStatus;

// 모든 API 관련 커스텀 예외의 기본 클래스
public abstract class ApiException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private String debugMessage;

    public ApiException(HttpStatus httpStatus, int code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public ApiException(HttpStatus httpStatus, int code, String message, String debugMessage) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.debugMessage = debugMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getDebugMessage() {
        return debugMessage;
    }
}
