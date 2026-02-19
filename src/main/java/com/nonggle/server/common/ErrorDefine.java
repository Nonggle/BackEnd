package com.nonggle.server.common;

import org.springframework.http.HttpStatus;

public enum ErrorDefine {
    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40101, "인증되지 않은 사용자입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 40102, "AccessToken이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 40103, "AccessToken이 유효하지 않습니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, 40104, "RefreshToken이 누락되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 40105, "RefreshToken이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 40106, "RefreshToken이 유효하지 않습니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, 40301, "접근 권한이 없습니다."),

    // 404 NOT FOUND
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "이력서를 찾을 수 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, 40402, "파일을 찾을 수 없습니다."),

    // 400 BAD REQUEST
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 40001, "잘못된 요청입니다."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "서버 내부 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorDefine(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
