package com.example.backend.securitylogin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 📌 Redis 기반 Refresh Token 관리 서비스
 * - JWT 인증 방식에서 사용자의 Refresh Token을 Redis에 저장
 * - Redis TTL을 활용하여 자동 만료 설정
 */
@Service // 🔹 Spring의 Service 컴포넌트로 등록
@RequiredArgsConstructor // 🔹 Lombok을 사용하여 생성자 주입 자동화
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate; // 🔹 RedisTemplate 주입

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:"; // 🔹 Redis Key Prefix

    /**
     * 🔹 Refresh Token을 Redis에 저장하는 메서드
     * - 사용자의 email을 Key로, Refresh Token을 Value로 저장
     * - TTL(만료 시간) 설정을 통해 자동 삭제되도록 구성
     */
    public void saveRefresh(String email, Integer expireS, String refresh) {
        String key = REFRESH_TOKEN_PREFIX + email; // 🔹 Redis 저장 Key (ex: refreshToken:user@email.com)
        redisTemplate.opsForValue().set(key, refresh, expireS, TimeUnit.SECONDS); // 🔹 TTL 설정하여 저장
    }

    /**
     * 🔹 Refresh Token 조회 메서드
     * - Redis에서 해당 email의 Refresh Token을 가져옴
     */
    public String getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        return redisTemplate.opsForValue().get(key); // 🔹 존재하지 않으면 null 반환
    }

    /**
     * 🔹 Refresh Token 삭제 메서드
     * - email 기반으로 삭제
     */
    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 🔹 Refresh Token 삭제 메서드 (토큰 기반)
     * - 토큰 값을 기준으로 삭제하는 방식
     */
    public void deleteRefreshTokenByToken(String refreshToken) {
        String keyPattern = REFRESH_TOKEN_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(keyPattern);
        if (keys != null) {
            for (String key : keys) {
                String storedToken = redisTemplate.opsForValue().get(key);
                if (refreshToken.equals(storedToken)) {
                    redisTemplate.delete(key);
                    break;
                }
            }
        }
    }
}
