package com.example.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 📌 JWT 토큰 생성 및 검증 유틸 클래스
 * - JWT를 생성하여 사용자 정보를 포함한 토큰을 발급
 * - JWT를 파싱하여 유효성 검사 및 사용자 정보 추출
 */
@Component // 🔹 Spring이 관리하는 Bean으로 등록
public class JWTUtil {

    private final SecretKey secretKey; // 🔹 JWT 서명을 위한 SecretKey

    /**
     * 🔹 JWT 서명을 위한 SecretKey 초기화
     * - application.yml에서 설정된 `spring.jwt.secret` 값을 가져와 SecretKey 생성
     */
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        String algorithm = Jwts.SIG.HS256.key().build().getAlgorithm();
        System.out.println("algorithm = " + algorithm);
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * 🔹 JWT의 Payload(클레임) 추출
     * - JWT를 파싱하여 클레임(Claims) 객체 반환
     */
    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // 🔹 SecretKey로 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 🔹 토큰에서 사용자 아이디(username) 추출
     */
    public String getUsername(String token) {
        return getPayload(token).get("username", String.class);
    }

    /**
     * 🔹 토큰에서 사용자 email 추출
     */
    public String getEmail(String token) {
        return getPayload(token).get("email", String.class);
    }

    /**
     * 🔹 토큰에서 사용자 역할(role) 추출
     */
    public String getRole(String token) {
        return getPayload(token).get("role", String.class);
    }

    /**
     * 🔹 토큰의 카테고리(category) 정보 추출
     * - "access" 또는 "refresh" 토큰을 구분하기 위함
     */
    public String getCategory(String token) {
        return getPayload(token).get("category", String.class);
    }

    /**
     * 🔹 토큰의 만료 여부 확인
     * - 현재 시간과 토큰의 만료 시간을 비교하여 만료 여부 반환
     */
    public Boolean isExpired(String token) {
        return getPayload(token).getExpiration().before(new Date());
    }

    /**
     * 🔹 JWT 생성 메서드
     * - category: "access" 또는 "refresh" (토큰 타입 구분)
     * - username: 사용자 아이디
     * - role: 사용자 권한 (ex: ROLE_USER, ROLE_ADMIN)
     * - expiredMs: 만료 시간 (밀리초 단위)
     */
    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category) // 🔹 토큰 타입 (access / refresh)
                .claim("username", username) // 🔹 사용자 아이디
                .claim("role", role) // 🔹 사용자 권한
                .issuedAt(new Date(System.currentTimeMillis())) // 🔹 토큰 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 🔹 토큰 만료 시간
                .signWith(secretKey) // 🔹 서명 추가
                .compact();
    }

    /**
     * 🔹 JWT 생성 메서드 (이메일 정보 포함)
     * - category: "access" 또는 "refresh" (토큰 타입 구분)
     * - username: 사용자 아이디
     * - email: 사용자 이메일
     * - role: 사용자 권한 (ex: ROLE_USER, ROLE_ADMIN)
     * - expiredMs: 만료 시간 (밀리초 단위)
     */
    public String createJwt(String category, String username, String email, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category) // 🔹 토큰 타입 (access / refresh)
                .claim("username", username) // 🔹 사용자 아이디
                .claim("email", email) // 🔹 사용자 이메일
                .claim("role", role) // 🔹 사용자 권한
                .issuedAt(new Date(System.currentTimeMillis())) // 🔹 토큰 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 🔹 토큰 만료 시간
                .signWith(secretKey) // 🔹 서명 추가
                .compact();
    }

    // ⚠️ Refresh Token은 일반적으로 데이터베이스에 저장하여 관리
    // ⚠️ Access Token은 클라이언트 측에서 관리 (쿠키 또는 localStorage)
    // ⚠️ JWT 서명에 사용되는 SecretKey는 보안이 중요하며, 환경 변수로 관리하는 것이 좋음
}
