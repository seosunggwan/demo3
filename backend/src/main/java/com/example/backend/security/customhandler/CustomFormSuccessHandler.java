package com.example.backend.security.customhandler;

import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.service.RefreshTokenService;
import com.example.backend.security.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 📌 폼 로그인 성공 후 JWT 발급 처리
 * - Access Token → HTTP 헤더에 저장
 * - Refresh Token → 쿠키에 저장
 */
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class CustomFormSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸 클래스
    private final RefreshTokenService refreshTokenService; // Refresh 토큰 관리 서비스

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 🔹 사용자 정보 가져오기
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 🔹 Access Token 생성 (10분 유효)
        String access_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, email, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
        response.setHeader(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, access_token); // Access Token을 응답 헤더에 추가

        // 🔹 Refresh Token 생성 (24시간 유효)
        String refresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, email, email, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        response.addCookie(CookieUtil.createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, refresh_token, (int)(TokenConstants.REFRESH_TOKEN_REDIS_TTL))); // Refresh Token을 쿠키에 저장

        // 🔹 Refresh Token을 DB에 저장
        refreshTokenService.saveRefresh(email, (int)TokenConstants.REFRESH_TOKEN_REDIS_TTL, refresh_token);

        // 🔹 JSON 응답 반환 (사용자 이름 포함)
        Map<String, Object> responseData = new HashMap<>();
        responseData.put(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, access_token);  // ✅ JWT 토큰 추가
        responseData.put(TokenConstants.TOKEN_CLAIM_EMAIL, email);
        responseData.put(TokenConstants.TOKEN_CLAIM_ROLE, role); // ✅ 역할 추가

        new ObjectMapper().writeValue(response.getWriter(), responseData);
    }
}
