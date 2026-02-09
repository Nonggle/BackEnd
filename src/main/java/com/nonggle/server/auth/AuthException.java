package com.nonggle.server.auth;

import com.nonggle.server.common.ApiException;
import org.springframework.http.HttpStatus;

// 인증 관련 예외를 처리하는 커스텀 예외 클래스
public class AuthException extends ApiException {

    public enum AuthError {
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "인증되지 않았습니다."),
        TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 401, "AccessToken이 만료되었습니다."),
        TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 401, "AccessToken이 유효하지 않습니다."),
        REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, 401, "RefreshToken이 필요합니다."),
        REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 401, "RefreshToken이 만료되었습니다."),
        REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 401, "RefreshToken이 유효하지 않습니다.");

        private final HttpStatus httpStatus;
        private final int code;
        private final String message;

        AuthError(HttpStatus httpStatus, int code, String message) {
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

    public AuthException(AuthError error) {
        super(error.getHttpStatus(), error.getCode(), error.getMessage());
    }

    public AuthException(AuthError error, String debugMessage) {
        super(error.getHttpStatus(), error.getCode(), error.getMessage(), debugMessage);
    }
}
