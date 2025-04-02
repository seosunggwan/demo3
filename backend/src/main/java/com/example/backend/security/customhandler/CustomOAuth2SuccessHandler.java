package com.example.backend.security.customhandler;

import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.dto.oauth2.CustomOAuth2User;
import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.service.RefreshTokenService;
import com.example.backend.security.service.oauth2.OAuthUserEntityToUserEntityService;
import com.example.backend.security.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import com.example.backend.security.entity.UserEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * OAuth2 로그인 성공 후 JWT 발급
 * access, refresh -> httpOnly 쿠키
 * 리다이렉트 되기 때문에 헤더로 전달 불가능
 *
 * ✅ 추가 설명:
 *  - OAuth2 로그인 완료 후 JWT를 생성하여 클라이언트에 전달
 *  - Access Token: 10분 유효, httpOnly 쿠키에 저장
 *  - Refresh Token: 24시간 유효, httpOnly 쿠키에 저장 (DB에도 저장)
 *  - 리다이렉트 방식으로 프론트엔드에 전달하여 보안 강화
 */
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = Logger.getLogger(CustomOAuth2SuccessHandler.class.getName());
    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸 클래스
    private final RefreshTokenService refreshTokenService; // Refresh 토큰 관리 서비스
    private final OAuthUserEntityToUserEntityService oAuthUserService; // UserEntity 동기화 서비스

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 🔹 OAuth2 로그인된 사용자 정보 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String name = customOAuth2User.getName(); // 실제 사용자 이름
        String username = customOAuth2User.getUsername(); // DB 저장용 사용자 ID (provider id)
        String email = customOAuth2User.getEmail(); // 사용자 이메일
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 🔹 디버깅 로그 추가
        logger.info("=== OAuth2 로그인 성공 정보 ===");
        logger.info("이름: " + name);
        logger.info("사용자 ID: " + username);
        logger.info("이메일: " + email);
        logger.info("역할: " + role);
        logger.info("===========================");

        // 🔹 UserEntity에도 OAuth2 사용자 정보 저장 (채팅 기능 등을 위해) - 개선된 서비스 사용
        UserEntity userEntity = oAuthUserService.syncOAuth2UserToUserEntity(username, name, email);
        logger.info("UserEntity 동기화 완료: " + userEntity.getEmail());

        // 🔹 JWT 생성
        String userEmail = userEntity.getEmail();
        
        // 이메일 정보를 포함하여 JWT 토큰 생성
        String access_token = jwtUtil.createJwt(TokenConstants.ACCESS_TOKEN_CATEGORY, username, userEmail, role, TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME);
        String refresh_token = jwtUtil.createJwt(TokenConstants.REFRESH_TOKEN_CATEGORY, username, userEmail, role, TokenConstants.REFRESH_TOKEN_EXPIRATION_TIME);

        // 🔹 Refresh Token을 DB에 저장하여 보안 강화 (세션 관리 대체)
        refreshTokenService.saveRefresh(userEmail, (int)TokenConstants.REFRESH_TOKEN_REDIS_TTL, refresh_token);

        // 🔹 JWT를 httpOnly 쿠키에 저장하여 XSS 공격 방지
        response.addCookie(CookieUtil.createCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, access_token, (int)(TokenConstants.ACCESS_TOKEN_EXPIRATION_TIME / 1000)));
        response.addCookie(CookieUtil.createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, refresh_token, (int)TokenConstants.REFRESH_TOKEN_REDIS_TTL));

        // 🔹 리다이렉트 시 사용자 이름과 이메일을 URL 파라미터로 전달하여 프론트엔드에서 활용 가능
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String encodedEmail = "";
        
        // userEntity에서 실제 이메일 가져오기
        if (userEntity.getEmail() != null && !userEntity.getEmail().isEmpty()) {
            encodedEmail = URLEncoder.encode(userEntity.getEmail(), "UTF-8");
        } else {
            // 이메일이 없는 경우 username을 그대로 사용
            encodedEmail = URLEncoder.encode(username, "UTF-8");
        }
        
        response.sendRedirect("http://localhost:5173/oauth2-jwt-header?name=" + encodedName + "&email=" + encodedEmail);
    }
}