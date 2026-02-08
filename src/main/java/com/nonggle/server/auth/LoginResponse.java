package com.nonggle.server.auth;

public record LoginResponse(
        Long userId,
        String nickname,
        String accessToken
) {}
