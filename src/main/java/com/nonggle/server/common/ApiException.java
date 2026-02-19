package com.nonggle.server.common;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorDefine error;
    private String customMessage;

    public ApiException(ErrorDefine error) {
        super(error.getMessage());
        this.error = error;
    }

    public ApiException(ErrorDefine error, String customMessage) {
        super(customMessage);
        this.error = error;
        this.customMessage = customMessage;
    }
}
