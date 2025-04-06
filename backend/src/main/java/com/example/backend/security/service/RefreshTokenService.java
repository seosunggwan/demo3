package com.example.backend.security.service;

import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
    private final JWTUtil jwtUtil;

    /**
     * 🔹 Refresh Token을 Redis에 저장하는 메서드
     * - 사용자의 email을 Key로, Refresh Token을 Value로 저장
     * - TTL(만료 시간) 설정을 통해 자동 삭제되도록 구성
     */
    public void saveRefresh(String email, Integer expireS, String refresh) {
        String key = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + email; // 🔹 Redis 저장 Key (ex: refreshToken:user@email.com)
        redisTemplate.opsForValue().set(key, refresh, expireS, TimeUnit.SECONDS); // 🔹 TTL 설정하여 저장
    }

    /**
     * 🔹 Refresh Token 조회 메서드
     * - Redis에서 해당 email의 Refresh Token을 가져옴
     */
    public String getRefreshToken(String email) {
        String key = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + email;
        return redisTemplate.opsForValue().get(key); // 🔹 존재하지 않으면 null 반환
    }

    /**
     * 🔹 Refresh Token 삭제 메서드
     * - email 기반으로 삭제
     */
    public void deleteRefreshToken(String email) {
        String key = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 🔹 Refresh Token 삭제 메서드 (토큰 기반)
     * - 토큰에서 이메일을 추출하여 삭제
     */
    public void deleteRefreshTokenByToken(String refreshToken) {
        try {
            String email = jwtUtil.getEmail(refreshToken);
            if (email == null) {
                System.out.println("토큰에서 이메일 추출 실패");
                return;
            }
            
            String key = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + email;
            System.out.println("삭제 시도할 Redis key: " + key);
            
            // Redis에 해당 key가 존재하는지 확인
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                redisTemplate.delete(key);
                System.out.println("Redis에서 토큰 삭제 성공: " + key);
            } else {
                System.out.println("Redis에 해당 key가 존재하지 않음: " + key);
            }
        } catch (Exception e) {
            System.err.println("토큰 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
