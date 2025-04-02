package com.example.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenService refreshTokenService; // Redis 기반 Refresh Token 관리 서비스

    /**
     * 🔹 로그아웃 메서드
     * - Redis에서 해당 사용자의 Refresh Token을 삭제
     */
    @Transactional
    public void logout(String username) {
        refreshTokenService.deleteRefreshToken(username); // ✅ username을 기반으로 삭제하도록 변경
    }

    /**
     * 🔹 토큰 값으로 로그아웃 처리하는 메서드
     * - 리프레시 토큰 값을 기반으로 Redis에서 해당 토큰을 직접 삭제
     */
    @Transactional
    public void logoutByToken(String refreshToken) {
        refreshTokenService.deleteRefreshTokenByToken(refreshToken);
    }
}
