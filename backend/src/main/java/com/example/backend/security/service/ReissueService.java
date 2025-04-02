package com.example.backend.security.service;

import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 📌 Redis 기반 Access Token 재발급 서비스
 * - Redis에서 Refresh Token을 검증하고 새로운 Access Token을 생성하여 반환
 * - Refresh Token Rotation 적용: 기존 Redis Token 폐기 후 새로운 Refresh Token 저장
 */
@Service // 🔹 Spring의 Service 컴포넌트로 등록
@RequiredArgsConstructor // 🔹 Lombok을 사용하여 생성자 주입 자동화
public class ReissueService {

    private final JWTUtil jwtUtil; // 🔹 JWT 생성 및 검증 유틸 클래스
    private final RefreshTokenService refreshTokenService; // 🔹 Redis 기반 Refresh Token 관리 서비스

    /**
     * 🔹 Refresh Token을 검증하고 새로운 Access Token을 발급하는 메서드
     * - Refresh Token이 유효하면 새로운 Access Token과 Refresh Token 발급
     * - 기존 Refresh Token은 폐기하고 새로 저장 (Refresh Token Rotation 적용)
     */
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh_token = null;
        Cookie[] cookies = request.getCookies();

        // 🔹 쿠키에서 Refresh Token 찾기
        refresh_token = Arrays.stream(cookies)
                .filter((cookie) -> cookie.getName().equals(TokenConstants.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        // 🔹 Refresh Token이 존재하지 않으면 400 Bad Request 반환
        if (refresh_token == null) {
            return new ResponseEntity<>(TokenConstants.TOKEN_NULL_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        // 🔹 만료된 Refresh Token 검증 (만료된 경우 ExpiredJwtException 발생)
        try {
            jwtUtil.isExpired(refresh_token);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>(TokenConstants.TOKEN_EXPIRED_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        // 🔹 Refresh Token이 아닌 경우 (유효한 Refresh Token인지 확인)
        String category = jwtUtil.getCategory(refresh_token);
        if (!category.equals(TokenConstants.REFRESH_TOKEN_CATEGORY)) {
            return new ResponseEntity<>(TokenConstants.TOKEN_INVALID_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        // 🔹 Refresh Token에서 사용자 정보(아이디, 역할) 추출
        String username = jwtUtil.getUsername(refresh_token);
        String role = jwtUtil.getRole(refresh_token);
        
        // 이메일 정보 추출 (없으면 username을 이메일로 사용)
        String email;
        try {
            email = jwtUtil.getEmail(refresh_token);
            if (email == null || email.isEmpty()) {
                email = username; // 이메일이 없으면 username을 사용
            }
        } catch (Exception e) {
            email = username; // 예외 발생 시 username을 이메일로 사용
        }

        // 🔹 Redis에서 Refresh Token 존재 여부 확인
        String storedRefreshToken = refreshTokenService.getRefreshToken(email);

        // 🔹 Redis에 저장된 Refresh Token과 비교 (없거나 다르면 무효화)
        if (storedRefreshToken == null || !storedRefreshToken.equals(refresh_token)) {
            return new ResponseEntity<>(TokenConstants.TOKEN_INVALID_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        // 🔹 새로운 Access Token 및 Refresh Token 생성
        String newAccess_token;
        String newRefresh_token;
        
        // 이메일이 있는 경우와 없는 경우를 구분하여 처리
        Integer expiredS = (int)TokenConstants.REFRESH_TOKEN_REDIS_TTL; // Refresh Token 유효기간: 24시간
        
        try {
            newAccess_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, username, email, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
            newRefresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, username, email, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        } catch (Exception e) {
            // 이메일 정보가 포함되지 않은 토큰 생성
            newAccess_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, username, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
            newRefresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, username, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        }

        // 🔹 기존 Refresh Token을 Redis에서 삭제하고 새 Refresh Token 저장 (Refresh Token Rotation 적용)
        refreshTokenService.deleteRefreshToken(email); // 기존 Refresh Token 삭제
        refreshTokenService.saveRefresh(email, expiredS, newRefresh_token); // 새로운 Refresh Token 저장

        // 🔹 새로운 Access Token을 헤더에 추가
        response.setHeader(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, newAccess_token);

        // 🔹 새로운 Refresh Token을 쿠키에 저장
        response.addCookie(CookieUtil.createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, newRefresh_token, expiredS));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
