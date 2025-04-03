package com.example.backend.security.service;

import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh_token = null;
        Cookie[] cookies = request.getCookies();

        // 쿠키 이름을 TokenConstants.REFRESH_TOKEN_COOKIE_NAME로 사용
        refresh_token = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TokenConstants.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refresh_token == null) {
            return new ResponseEntity<>(TokenConstants.TOKEN_NULL_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.isExpired(refresh_token);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>(TokenConstants.TOKEN_EXPIRED_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        String category = jwtUtil.getCategory(refresh_token);
        if (!category.equals(TokenConstants.REFRESH_TOKEN_CATEGORY)) {
            return new ResponseEntity<>(TokenConstants.TOKEN_INVALID_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh_token);
        String role = jwtUtil.getRole(refresh_token);

        String email;
        try {
            email = jwtUtil.getEmail(refresh_token);
            if (email == null || email.isEmpty()) {
                email = username;
            }
        } catch (Exception e) {
            email = username;
        }

        String storedRefreshToken = refreshTokenService.getRefreshToken(email);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refresh_token)) {
            return new ResponseEntity<>(TokenConstants.TOKEN_INVALID_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        String newAccess_token;
        String newRefresh_token;
        Integer expiredS = (int) TokenConstants.REFRESH_TOKEN_REDIS_TTL;

        try {
            newAccess_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, username, email, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
            newRefresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, username, email, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        } catch (Exception e) {
            newAccess_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, username, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
            newRefresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, username, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        }

        // Refresh Token Rotation 적용: 기존 토큰 삭제 후 새 토큰 저장
        refreshTokenService.deleteRefreshToken(email);
        refreshTokenService.saveRefresh(email, expiredS, newRefresh_token);

        response.setHeader(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, newAccess_token);
        response.addCookie(CookieUtil.createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, newRefresh_token, expiredS));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
