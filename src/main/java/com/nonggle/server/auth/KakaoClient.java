package com.nonggle.server.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;
import okhttp3.*;
import java.io.IOException;

@Component
public class KakaoClient {

    private static final String KAKAO_USER_API = "https://kapi.kakao.com/v2/user/me";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param accessToken Android에서 전달받은 Kakao access token
     */
    public KakaoUser getUserInfo(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            // 프론트/서버 입력값 문제
            throw new KakaoAuthException(KakaoAuthError.INVALID_INPUT, "Kakao accessToken is missing");
        }

        Request request = new Request.Builder()
                .url(KAKAO_USER_API)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .build();

        try (Response response = client.newCall(request).execute()) {

            int status = response.code();
            String bodyStr = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                // 카카오 토큰 문제(대부분 401) / 권한 문제(403) / 카카오 서버 문제(5xx) 등
                KakaoAuthError error = mapError(status);
                throw new KakaoAuthException(
                        error,
                        "Kakao /v2/user/me failed. status=" + status + ", body=" + safeTrim(bodyStr)
                );
            }

            JsonNode root = mapper.readTree(bodyStr);

            // id는 필수 (없으면 응답 자체가 이상한 것)
            JsonNode idNode = root.get("id");
            if (idNode == null) {
                throw new KakaoAuthException(
                        KakaoAuthError.INVALID_RESPONSE,
                        "Kakao response missing 'id'. body=" + safeTrim(bodyStr)
                );
            }

            String id = idNode.asText();

            return new KakaoUser(id);

        } catch (KakaoAuthException e) {
            // 이미 의미 있는 예외면 그대로 던짐
            throw e;
        } catch (IOException e) {
            // 네트워크/파싱 문제: 서버 내부 오류로 볼지, 외부 연동 실패로 볼지 선택 가능
            throw new KakaoAuthException(KakaoAuthError.IO_ERROR, "Kakao API IO error", e);
        } catch (Exception e) {
            // 예상 못한 런타임 문제
            throw new KakaoAuthException(KakaoAuthError.UNKNOWN, "Unexpected Kakao client error", e);
        }
    }

    private KakaoAuthError mapError(int status) {
        if (status == 401) return KakaoAuthError.UNAUTHORIZED; // 토큰 만료/무효 가능성 큼
        if (status == 403) return KakaoAuthError.FORBIDDEN;    // 권한/스코프 문제 가능성
        if (status >= 500) return KakaoAuthError.KAKAO_SERVER_ERROR;
        return KakaoAuthError.BAD_REQUEST; // 400 등
    }

    private String safeTrim(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    public record KakaoUser(String kakaoId) {}

    public enum KakaoAuthError {
        INVALID_INPUT,
        UNAUTHORIZED,
        FORBIDDEN,
        BAD_REQUEST,
        KAKAO_SERVER_ERROR,
        INVALID_RESPONSE,
        IO_ERROR,
        UNKNOWN
    }

    public static class KakaoAuthException extends RuntimeException {
        private final KakaoAuthError error;

        public KakaoAuthException(KakaoAuthError error, String message) {
            super(message);
            this.error = error;
        }

        public KakaoAuthException(KakaoAuthError error, String message, Throwable cause) {
            super(message, cause);
            this.error = error;
        }

        public KakaoAuthError getError() {
            return error;
        }
    }
}
