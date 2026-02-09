package com.nonggle.server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(String kakaoId);
    Optional<User> findByRefreshToken(String refreshToken); // RefreshToken으로 사용자 조회 메서드 추가
}
