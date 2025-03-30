package com.example.backend.securitylogin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * 📌 Redis 기반 Refresh Token 엔터티
 * - TTL(Time-To-Live) 사용하여 자동 만료
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 604800) // 7일(초 단위) 후 자동 삭제 (604800초 = 7일)
public class RefreshEntity {

    @Id
    private String refresh; // 🔹 Redis의 Key 값 (Refresh Token)

    private String username; // 🔹 해당 토큰이 속한 사용자
}
