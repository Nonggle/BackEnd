package com.nonggle.server.auth;

public record RefreshTokenRequest(
        String refreshToken
) {}
