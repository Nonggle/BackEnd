package com.nonggle.server.auth;

import com.nonggle.server.user.User;
import com.nonggle.server.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public AuthService(
            KakaoClient kakaoClient,
            UserRepository userRepository,
            JwtProvider jwtProvider
    ) {
        this.kakaoClient = kakaoClient;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse kakaoLogin(String accessToken) {
        // 1️⃣ 카카오 사용자 정보 조회
        KakaoUser kakaoUser = kakaoClient.getUserInfo(accessToken);

        // 2️⃣ DB에서 사용자 조회
        User user = userRepository.findByKakaoId(kakaoUser.id())
                .orElseGet(() ->
                        userRepository.save(
                                new User(
                                        kakaoUser.id(),
                                        kakaoUser.nickname()
                                )
                        )
                );
        String jwt = jwtProvider.createToken(user.getId());

        // 3️⃣ 응답 반환
        return new LoginResponse(
                user.getId(),
                user.getNickname(),
                jwt
        );
    }
}
