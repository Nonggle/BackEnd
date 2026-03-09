package com.nonggle.server.auth;

import com.nonggle.server.auth.KakaoClient.KakaoUser;
import com.nonggle.server.user.User;
import com.nonggle.server.user.UserRepository;
import com.nonggle.server.common.ApiException;
import com.nonggle.server.common.ErrorDefine;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.Clock;

// 인증 비즈니스 로직

@Service
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final Clock clock;

    public AuthService(
            KakaoClient kakaoClient,
            UserRepository userRepository,
            JwtProvider jwtProvider,
            Clock clock
    ) {
        this.kakaoClient = kakaoClient;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.clock = clock;
    }

    public LoginResponse kakaoLogin(String accessToken) {
        // 1️⃣ 카카오 사용자 정보 조회
        KakaoUser kakaoUser = kakaoClient.getUserInfo(accessToken);

        // 2️⃣ DB에서 사용자 조회 및 리프레시 토큰 발급/갱신
        User user = userRepository.findByKakaoId(kakaoUser.kakaoId())
                .orElseGet(() ->
                        userRepository.save(
                                new User(
                                        kakaoUser.kakaoId()
                                )
                        )
                );

        String newJwtAccessToken = jwtProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        Instant newRefreshTokenExpiryDate = Instant.now(clock).plus(14, ChronoUnit.DAYS); // 2주 후 만료 시각 설정
        user.updateRefreshToken(newRefreshToken, newRefreshTokenExpiryDate);
        userRepository.save(user);

        // 3️⃣ 응답 반환
        return new LoginResponse(
                user.getId(),
                newJwtAccessToken,
                newRefreshToken
        );
    }

    public LoginResponse refreshToken(String refreshToken) {
        // 1️⃣ RefreshToken 유효성 검사
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ErrorDefine.REFRESH_TOKEN_MISSING);
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorDefine.REFRESH_TOKEN_INVALID));

        if (user.getRefreshTokenExpiryDate() == null || Instant.now(clock).isAfter(user.getRefreshTokenExpiryDate())) {
            throw new ApiException(ErrorDefine.REFRESH_TOKEN_EXPIRED);
        }

        // 2️⃣ 새로운 AccessToken 및 RefreshToken 발급 (RefreshToken Rotation)
        String newAccessToken = jwtProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        Instant newRefreshTokenExpiryDate = Instant.now(clock).plus(14, ChronoUnit.DAYS);

        user.updateRefreshToken(newRefreshToken, newRefreshTokenExpiryDate);
        userRepository.save(user);

        // 3️⃣ 응답 반환
        return new LoginResponse(
                user.getId(),
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorDefine.UNAUTHORIZED));
        user.invalidateRefreshToken();
        userRepository.save(user);
    }
}
