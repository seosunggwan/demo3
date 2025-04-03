package com.example.backend.security.customhandler;

import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.repository.RefreshRepository;
import com.example.backend.security.util.CookieUtil;
import com.example.backend.security.constant.TokenConstants;
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

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

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

        // 쿠키 이름을 TokenConstants를 사용하여 통일
        String refresh = Arrays.stream(cookies)
                .filter(cookie -> TokenConstants.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String category = jwtUtil.getCategory(refresh);
        if (category == null || !category.equals(TokenConstants.REFRESH_TOKEN_CATEGORY)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean isExist = refreshRepository.existsById(refresh);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String redisKey = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + refresh;
        System.out.println("Deleting refresh token: " + redisKey);
        refreshRepository.deleteById(redisKey);
        System.out.println("Refresh token deleted.");

        // 쿠키 이름을 TokenConstants를 사용하여 생성
        Cookie cookie = CookieUtil.createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, null, 0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
