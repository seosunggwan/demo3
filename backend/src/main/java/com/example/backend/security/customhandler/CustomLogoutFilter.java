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

        if (!requestURI.matches("^\\/auth\\/logout$")) {
            chain.doFilter(request, response);
            return;
        }

        if (!"POST".equals(requestMethod)) {
            chain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TokenConstants.REFRESH_TOKEN_COOKIE_NAME)) {
                    String refreshToken = cookie.getValue();
                    if (refreshToken != null) {
                        // Redis에서 refresh token 삭제
                        String redisKey = TokenConstants.REFRESH_TOKEN_REDIS_PREFIX + refreshToken;
                        refreshRepository.deleteById(redisKey);
                    }
                    // 쿠키 삭제 - CookieUtil.deleteCookie() 사용
                    response.addCookie(CookieUtil.deleteCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME));
                    break;
                }
            }
        }

        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Logged out successfully");
    }
}
