package com.nonggle.server.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;

@Component
public class KakaoClient {

    private static final String KAKAO_USER_API =
            "https://kapi.kakao.com/v2/user/me";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public KakaoUser getUserInfo(String accessToken) {
        Request request = new Request.Builder()
                .url(KAKAO_USER_API)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("카카오 인증 실패");
            }

            JsonNode body = mapper.readTree(response.body().string());

            String id = body.get("id").asText();
            String nickname = body
                    .get("properties")
                    .get("nickname")
                    .asText();

            return new KakaoUser(id, nickname);

        } catch (Exception e) {
            throw new IllegalArgumentException("카카오 사용자 정보 조회 실패");
        }
    }
}
