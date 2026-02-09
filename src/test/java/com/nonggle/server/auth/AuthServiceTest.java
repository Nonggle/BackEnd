package com.nonggle.server.auth;

import com.nonggle.server.auth.KakaoClient.KakaoAuthException;
import com.nonggle.server.auth.KakaoClient.KakaoUser;
import com.nonggle.server.user.User;
import com.nonggle.server.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // ArgumentCaptor import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant; // Instant import
import java.time.temporal.ChronoUnit; // ChronoUnit import
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_KAKAO_ACCESS_TOKEN = "test_kakao_access_token";
    private static final String TEST_KAKAO_USER_ID = "12345";
    private static final String TEST_JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        // userRepository.save()의 thenAnswer 설정을 BeforeEach로 이동하여 중복 제거 및 일관성 유지
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User userToSave = invocation.getArgument(0);
                if (userToSave.getId() == null) { // ID가 없는 경우 (첫 번째 저장)
                    userToSave.setId(1L); // 테스트용 ID 할당
                }
                return userToSave; // 변경된 User 객체를 그대로 반환
            });
    }

    @Test
    @DisplayName("성공적인 카카오 로그인 - 기존 사용자")
    void kakaoLogin_existingUser_success() {
        // Given
        // Mock KakaoClient to return a KakaoUser for successful scenarios
        when(kakaoClient.getUserInfo(TEST_KAKAO_ACCESS_TOKEN))
                .thenReturn(new KakaoUser(TEST_KAKAO_USER_ID));
        // Mock JwtProvider to return a test JWT token for this specific test
        when(jwtProvider.createToken(any(Long.class)))
                .thenReturn(TEST_JWT_TOKEN);

        User existingUser = new User(TEST_KAKAO_USER_ID);
        existingUser.updateRefreshToken("old_refresh_token", Instant.now().plus(1, ChronoUnit.DAYS)); // 기존 리프레시 토큰 설정
        existingUser.setId(1L); // ID를 미리 설정
        
        when(userRepository.findByKakaoId(TEST_KAKAO_USER_ID))
                .thenReturn(Optional.of(existingUser));

        // When
        LoginResponse response = authService.kakaoLogin(TEST_KAKAO_ACCESS_TOKEN);

        // Then
        assertThat(response.userId()).isEqualTo(existingUser.getId());
        assertThat(response.accessToken()).isEqualTo(TEST_JWT_TOKEN);
        assertThat(response.refreshToken()).isNotNull(); // 리프레시 토큰이 반환되는지 확인

        // `AuthService`에서 리프레시 토큰을 업데이트하고 저장하므로 save 호출됨
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRefreshToken()).isEqualTo(response.refreshToken());
        assertThat(savedUser.getRefreshTokenExpiryDate()).isNotNull();
        assertThat(savedUser.getRefreshTokenExpiryDate()).isAfter(Instant.now()); // 만료일이 현재 시각보다 미래인지 확인
        assertThat(savedUser.getRefreshTokenExpiryDate().minus(2, ChronoUnit.WEEKS)).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5)); // 대략 2주 후인지 확인
    }

    @Test
    @DisplayName("성공적인 카카오 로그인 - 신규 사용자")
    void kakaoLogin_newUser_success() {
        // Given
        // Mock KakaoClient to return a KakaoUser for successful scenarios
        when(kakaoClient.getUserInfo(TEST_KAKAO_ACCESS_TOKEN))
                .thenReturn(new KakaoUser(TEST_KAKAO_USER_ID));
        // Mock JwtProvider to return a test JWT token for this specific test
        when(jwtProvider.createToken(any(Long.class)))
                .thenReturn(TEST_JWT_TOKEN);

        when(userRepository.findByKakaoId(TEST_KAKAO_USER_ID))
                .thenReturn(Optional.empty());
        // newUser는 ID가 없으므로 서비스 로직에서 저장될 때 ID가 할당될 것
        // userRepository.save() 목킹은 BeforeEach에 설정됨

        // When
        LoginResponse response = authService.kakaoLogin(TEST_KAKAO_ACCESS_TOKEN);

        // Then
        // ID는 userRepository.save가 할당했다고 가정하므로, response에서 직접 가져오거나 캡쳐된 User에서 가져와야 함
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture()); // 초기 저장 및 업데이트 저장
        User savedUserAfterSecondSave = userCaptor.getValue(); // 마지막으로 save된 User 객체

        assertThat(response.userId()).isEqualTo(savedUserAfterSecondSave.getId());
        assertThat(response.accessToken()).isEqualTo(TEST_JWT_TOKEN);
        assertThat(response.refreshToken()).isNotNull(); // 리프레시 토큰이 반환되는지 확인

        assertThat(savedUserAfterSecondSave.getRefreshToken()).isEqualTo(response.refreshToken());
        assertThat(savedUserAfterSecondSave.getRefreshTokenExpiryDate()).isNotNull();
        assertThat(savedUserAfterSecondSave.getRefreshTokenExpiryDate()).isAfter(Instant.now()); // 만료일이 현재 시각보다 미래인지 확인
        assertThat(savedUserAfterSecondSave.getRefreshTokenExpiryDate().minus(2, ChronoUnit.WEEKS)).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5)); // 대략 2주 후인지 확인
    }

    @Test
    @DisplayName("카카오 클라이언트에서 사용자 정보 조회 실패 시 예외 발생")
    void kakaoLogin_userInfoFetchFails_throwsException() {
        // Given
        when(kakaoClient.getUserInfo(anyString()))
                .thenThrow(new KakaoAuthException(KakaoClient.KakaoAuthError.UNKNOWN, "카카오 사용자 정보 조회 실패"));

        // When & Then
        KakaoAuthException exception = assertThrows(KakaoAuthException.class,
                () -> authService.kakaoLogin(TEST_KAKAO_ACCESS_TOKEN));

        assertThat(exception.getError()).isEqualTo(KakaoClient.KakaoAuthError.UNKNOWN);
        assertThat(exception.getMessage()).contains("카카오 사용자 정보 조회 실패");
        verify(userRepository, never()).findByKakaoId(anyString()); // 카카오 클라이언트 실패 시 DB 조회 안됨
        verify(userRepository, never()).save(any(User.class));
        verify(jwtProvider, never()).createToken(any(Long.class));
    }

    @Test
    @DisplayName("유효하지 않은 카카오 액세스 토큰으로 로그인 시도 시 예외 발생")
    void kakaoLogin_invalidKakaoAccessToken_throwsException() {
        // Given
        String invalidAccessToken = "invalid_token";
        when(kakaoClient.getUserInfo(invalidAccessToken))
                .thenThrow(new KakaoAuthException(KakaoClient.KakaoAuthError.UNAUTHORIZED, "AccessToken이 만료되었습니다."));

        // When & Then
        KakaoAuthException exception = assertThrows(KakaoAuthException.class,
                () -> authService.kakaoLogin(invalidAccessToken));

        assertThat(exception.getError()).isEqualTo(KakaoClient.KakaoAuthError.UNAUTHORIZED);
        assertThat(exception.getMessage()).contains("AccessToken이 만료되었습니다.");
        verify(userRepository, never()).findByKakaoId(anyString());
    }
}
