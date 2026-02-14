package com.nonggle.server.common;

import com.nonggle.server.auth.AuthException;
import com.nonggle.server.auth.KakaoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KakaoClient.KakaoAuthException.class)
    public ResponseEntity<ApiResponse<?>> handleKakaoAuthException(KakaoClient.KakaoAuthException e) {
        log.error("KakaoAuthException: Error - {}, Message - {}", e.getError(), e.getMessage(), e);

        AuthException.AuthError error = switch (e.getError()) {
            case UNAUTHORIZED, FORBIDDEN -> AuthException.AuthError.TOKEN_INVALID; // 401, "잘못된 카카오 토큰"
            default -> null;
        };

        if (error != null) {
            return ResponseEntity
                    .status(error.getHttpStatus())
                    .body(ApiResponse.fail(error.getCode(), "카카오 인증에 실패했습니다. 토큰을 확인해주세요."));
        }

        // 기본 처리 (500)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "카카오 연동 중 서버 오류가 발생했습니다."));
    }
    // ApiException 및 하위 클래스 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
        log.error("ApiException: HTTP Status - {}, Code - {}, Message - {}",
                e.getHttpStatus(), e.getCode(), e.getMessage(), e);
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // 예상치 못한 모든 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(500, "서버 내부 오류가 발생했습니다."));
    }
}
