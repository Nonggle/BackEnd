package com.nonggle.server.user;
import jakarta.persistence.*;
import java.time.Instant; // Instant import 추가

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String kakaoId;

    @Column(length = 500) // Adjust length as needed for your refresh token size
    private String refreshToken;

    private Instant refreshTokenExpiryDate; // refreshToken 만료 시각 필드 추가

    protected User() {}

    public User(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void updateRefreshToken(String newRefreshToken, Instant expiryDate) {
        this.refreshToken = newRefreshToken;
        this.refreshTokenExpiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    // 테스트 목적으로만 사용 (JPA 모범 사례 아님)
    public void setId(Long id) {
        this.id = id;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getRefreshTokenExpiryDate() {
        return refreshTokenExpiryDate;
    }
}
