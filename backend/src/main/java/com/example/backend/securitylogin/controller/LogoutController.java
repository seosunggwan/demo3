package com.example.backend.securitylogin.controller;

import com.example.backend.securitylogin.jwt.JWTUtil;
import com.example.backend.securitylogin.service.LogoutService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LogoutController {

    private final LogoutService logoutService;
    private final JWTUtil jwtUtil;

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return new ResponseEntity<>("No cookies found", HttpStatus.BAD_REQUEST);
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refresh_token"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token not found in cookies", HttpStatus.BAD_REQUEST);
        }

        // 리프레시 토큰에서 이메일 정보 추출 시도
        String email = null;
        try {
            email = jwtUtil.getEmail(refreshToken);
        } catch (Exception e) {
            // 이메일 정보 추출 실패 시 username 사용
            email = auth.getName();
        }
        
        // 이메일이 null이거나 비어있으면 사용자 이름을 이메일로 사용
        if (email == null || email.isEmpty()) {
            email = auth.getName();
        }

        // 이메일을 키로 사용하여 토큰 삭제
        logoutService.logout(email);

        SecurityContextHolder.clearContext(); // ✅ SecurityContext 초기화

        // 다양한 방식으로 쿠키 삭제 시도
        Cookie deleteCookie = new Cookie("refresh_token", null);
        deleteCookie.setMaxAge(0); // 쿠키 즉시 만료
        deleteCookie.setPath("/");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        response.addCookie(deleteCookie);
        
        // 도메인 속성을 명시적으로 지정하지 않은 쿠키 삭제
        Cookie deleteCookie2 = new Cookie("refresh_token", "");
        deleteCookie2.setMaxAge(0);
        deleteCookie2.setPath("/");
        response.addCookie(deleteCookie2);
        
        // 루트 경로 쿠키 삭제
        Cookie deleteCookie3 = new Cookie("refresh_token", "");
        deleteCookie3.setMaxAge(0);
        deleteCookie3.setPath("/");
        deleteCookie3.setDomain("localhost"); // localhost 도메인 명시
        response.addCookie(deleteCookie3);
        
        // 추가 헤더로 쿠키 삭제 지시
        response.setHeader("Set-Cookie", "refresh_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Strict");

        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }
}
