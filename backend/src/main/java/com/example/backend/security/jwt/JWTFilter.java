package com.example.backend.security.jwt;

import com.example.backend.security.dto.form.CustomUserDetails;
import com.example.backend.security.entity.Role;
import com.example.backend.security.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 📌 JWT 인증 필터
 * - HTTP 요청에서 JWT(Access Token)를 확인하고 검증하여 인증 정보를 설정
 * - 인증된 사용자의 정보를 `SecurityContextHolder`에 저장하여 이후 요청에서 사용 가능하도록 함
 */
@Component // 🔹 Spring이 자동으로 관리하는 Bean 등록
@RequiredArgsConstructor // 🔹 Lombok을 사용하여 생성자 주입 자동화
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil; // 🔹 JWT 생성 및 검증 유틸 클래스

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        System.out.println("\n🌐 요청 URI: " + requestURI);
        
        if (requestURI.equals("/oauth2-jwt-header")) {
            System.out.println("🔐 OAuth2 JWT 헤더 요청은 JWT 검증에서 제외됩니다.");
            filterChain.doFilter(request, response);
            return;
        }
        
        String access_token = request.getHeader("Authorization");
        System.out.println("🔍 요청받은 Authorization 헤더 값: " + access_token);

        if (access_token == null || !access_token.startsWith("Bearer ")) {
            System.out.println("🚨 JWT 토큰이 없거나 잘못된 형식임. 요청을 그대로 진행함.");
            filterChain.doFilter(request, response);
            return;
        }

        access_token = access_token.substring(7);
        System.out.println("🎫 처리할 토큰 값: " + access_token);

        try {
            System.out.println("🔎 토큰 만료 여부 확인 시작...");
            if (jwtUtil.isExpired(access_token)) {
                System.out.println("🚨 JWT 토큰이 만료됨");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT Token Expired");
                return;
            }
            System.out.println("✅ JWT 토큰이 유효함");

            String category = jwtUtil.getCategory(access_token);
            String username = jwtUtil.getUsername(access_token);
            String role = jwtUtil.getRole(access_token);

            System.out.println("📋 토큰 정보:");
            System.out.println("   - 카테고리: " + category);
            System.out.println("   - 사용자명: " + username);
            System.out.println("   - 권한: " + role);

            if (!"access_token".equals(category)) {
                System.out.println("🚨 JWT 토큰의 category가 'access_token'가 아님: " + category);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UserEntity userPrincipal = UserEntity.builder()
                    .email(username)
                    .role(Role.valueOf(role))
                    .password("temp_pw")
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(userPrincipal);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("🔑 인증 성공: " + username + " (Role: " + role + ")");
            System.out.println("🛡️ SecurityContextHolder에 저장된 인증 정보: " + SecurityContextHolder.getContext().getAuthentication());

        } catch (ExpiredJwtException e) {
            System.out.println("🚨 JWT 토큰 만료됨: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT Token Expired");
            return;
        } catch (SignatureException | MalformedJwtException e) {
            System.out.println("🚨 JWT 토큰이 손상되었거나 서명이 올바르지 않음: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT Signature");
            return;
        } catch (Exception e) {
            System.out.println("🚨 JWT 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT Error");
            return;
        }

        filterChain.doFilter(request, response);
        System.out.println("✨ 필터 체인 처리 완료\n");
    }
}
