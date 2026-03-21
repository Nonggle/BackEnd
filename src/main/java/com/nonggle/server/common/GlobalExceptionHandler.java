package com.nonggle.server.common;


import com.nonggle.server.auth.KakaoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KakaoClient.KakaoAuthException.class)
    public ResponseEntity<ApiResponse<?>> handleKakaoAuthException(KakaoClient.KakaoAuthException e) {
        log.error("KakaoAuthException: Error - {}, Message - {}", e.getError(), e.getMessage(), e);

        ErrorDefine error = switch (e.getError()) {
            case UNAUTHORIZED, FORBIDDEN -> ErrorDefine.TOKEN_INVALID;
            default -> ErrorDefine.INTERNAL_ERROR;
        };

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(ApiResponse.fail(error.getCode(), "카카오 인증에 실패했습니다. 토큰을 확인해주세요."));
    }

    // 비즈니스 로직 관련 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
        ErrorDefine errorDefine = e.getError();
        String message = e.getCustomMessage() == null ? errorDefine.getMessage() : e.getCustomMessage();

        log.error("ApiException: HTTP Status - {}, Code - {}, Message - {}",
                errorDefine.getHttpStatus(), errorDefine.getCode(), message, e);

        return ResponseEntity
                .status(errorDefine.getHttpStatus())
                .body(ApiResponse.fail(errorDefine.getCode(), message));
    }

    // 예상치 못한 모든 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
        log.error("Unhandled exception: ", e); // 에러 로그에 스택 트레이스 포함
        ErrorDefine errorDefine = ErrorDefine.INTERNAL_ERROR;
        return ResponseEntity
                .status(errorDefine.getHttpStatus())
                .body(ApiResponse.fail(errorDefine.getCode(), "서버 내부 오류가 발생했습니다. 로그를 확인해주세요. (" + e.getMessage() + ")"));
    }
}
