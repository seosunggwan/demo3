package com.example.backend.security.controller;

import com.example.backend.security.service.LogoutService;
import com.example.backend.security.constant.TokenConstants;
import com.example.backend.security.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("로그아웃 요청 시작");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("인증되지 않은 사용자");
            return new ResponseEntity<>("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("쿠키가 없음");
            return new ResponseEntity<>("No cookies found", HttpStatus.BAD_REQUEST);
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> TokenConstants.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            System.out.println("Refresh 토큰이 없음");
            return new ResponseEntity<>("Refresh token not found in cookies", HttpStatus.BAD_REQUEST);
        }

        System.out.println("Refresh 토큰 발견: " + refreshToken);

        // 토큰 값을 기반으로 로그아웃 처리
        System.out.println("LogoutService.logoutByToken 호출");
        logoutService.logoutByToken(refreshToken);
        System.out.println("LogoutService.logoutByToken 완료");

        SecurityContextHolder.clearContext();
        System.out.println("SecurityContext 초기화 완료");

        // 쿠키 삭제 - CookieUtil.deleteCookie() 사용
        response.addCookie(CookieUtil.deleteCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME));
        System.out.println("쿠키 삭제 완료");
        
        // 추가 헤더로 쿠키 삭제 지시
        response.setHeader("Set-Cookie", TokenConstants.REFRESH_TOKEN_COOKIE_NAME + "=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict");
        System.out.println("Set-Cookie 헤더 설정 완료");

        System.out.println("로그아웃 처리 완료");
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }
}
