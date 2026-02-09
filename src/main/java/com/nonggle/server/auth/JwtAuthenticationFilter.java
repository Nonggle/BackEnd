package com.nonggle.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonggle.server.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

import static com.nonggle.server.auth.AuthException.AuthError; // AuthError import

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> WHITELIST_PREFIX = Set.of(
            "/auth", "/hello", "/h2-console"
    );

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 프리플라이트(필요 시)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        return WHITELIST_PREFIX.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, AuthError.UNAUTHORIZED.getMessage()); // 메시지를 AuthError에서 가져옴
            return;
        }

        String token = authHeader.substring(7);

        try {
            Long userId = jwtProvider.getUserId(token);
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorized(response, AuthError.TOKEN_INVALID.getMessage()); // 메시지를 AuthError에서 가져옴
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(AuthError.UNAUTHORIZED.getCode(), message)); // ApiResponse.fail 사용
    }
}