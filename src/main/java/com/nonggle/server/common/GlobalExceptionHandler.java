package com.nonggle.server.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // Slf4j 어노테이션 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

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
