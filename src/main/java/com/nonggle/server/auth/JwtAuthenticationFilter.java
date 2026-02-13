package com.nonggle.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonggle.server.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//HTTP 요청의 토큰 인증 처리
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // PermitAll로 설정된 경로들
    private final List<String> publicPaths = Arrays.asList(
            "/auth/kakao",
            "/auth/token/refresh",
            "/hello",
            "/health",
            "/h2-console/**"
    );

    public JwtAuthenticationFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // public path의 경우 필터를 거치지 않음
        return publicPaths.stream()
                .anyMatch(p -> pathMatcher.match(p, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthException(AuthException.AuthError.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            Long userId = jwtProvider.getUserId(token); // AuthException will be thrown here for expired/invalid tokens

            // Set authentication in SecurityContext
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (AuthException e) {
            writeErrorResponse(response, e.getAuthError());
        }
    }

    private void writeErrorResponse(HttpServletResponse response, AuthException.AuthError error) throws IOException {
        response.setStatus(error.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(error.getCode(), error.getMessage()));
    }
}