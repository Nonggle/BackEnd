package com.nonggle.server.auth;

import com.nonggle.server.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/kakao")
    public ApiResponse<LoginResponse> kakaoLogin( // 카카오로그인 실동작 부분
            @RequestBody KakaoLoginRequest request
    ) {
        return ApiResponse.ok(
                authService.kakaoLogin(request.accessToken())
        );
    }
}
