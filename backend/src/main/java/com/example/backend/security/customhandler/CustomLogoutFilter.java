package com.example.backend.security.customhandler;

import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.repository.RefreshRepository;
import com.example.backend.security.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;

/**
 * 📌 Redis 기반 로그아웃 필터
 * - Refresh Token 만료 및 삭제
 * - Redis에서 Refresh Token 관리
 */
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository; // Redis 기반 Repository

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!requestURI.matches("^\\/logout$")) {
            chain.doFilter(request, response);
            return;
        }

        if (!("POST".equals(requestMethod) || "DELETE".equals(requestMethod))) {
            chain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String refresh = Arrays.stream(cookies)
                .filter(cookie -> "refresh".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String category = jwtUtil.getCategory(refresh);
        if (category == null || !category.equals("refresh")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 🔹 Redis에서 Refresh Token 존재 여부 확인
        boolean isExist = refreshRepository.existsById(refresh);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 🔥 Redis Key를 맞춰서 삭제
        String redisKey = "refreshToken:" + refresh;
        System.out.println("Deleting refresh token: " + redisKey);
        refreshRepository.deleteById(redisKey);
        System.out.println("Refresh token deleted.");

        // 🔹 쿠키에서 Refresh Token 삭제
        Cookie cookie = CookieUtil.createCookie("refresh", null, 0);
        response.addCookie(cookie);

        // 🔹 SecurityContext 초기화 (로그아웃 처리)
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
