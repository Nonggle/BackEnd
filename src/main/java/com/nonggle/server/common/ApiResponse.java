package com.nonggle.server.common;
// 공통 응답 클래스
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiResponse.Error error // error 객체로 변경
) {
    public record Error(int code, String message) {} // Error 내부 레코드 정의

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null); // error는 null로 설정
    }

    // 기존 error 메서드는 사용하지 않음

    public static ApiResponse<?> fail(int code, String message) {
        return new ApiResponse<>(false, null, new Error(code, message));
    }
}
