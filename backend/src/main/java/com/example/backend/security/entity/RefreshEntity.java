package com.example.backend.security.entity;

import com.example.backend.security.constant.TokenConstants;
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
@RedisHash(value = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX, timeToLive = TokenConstants.REFRESH_TOKEN_REDIS_TTL)
public class RefreshEntity {

    @Id
    private String refresh; // 🔹 Redis의 Key 값 (Refresh Token)

    private String email; // 🔹 해당 토큰이 속한 사용자의 이메일
}
