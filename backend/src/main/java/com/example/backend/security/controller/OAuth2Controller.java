package com.example.backend.security.controller;

import com.example.backend.security.service.oauth2.OAuth2JwtHeaderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2 로그인 시 바로 응답을 할 수 없기 때문에 리다이렉트 하여 쿠키로 토큰을 보냄 (액세스+리프레시)
 * httpOnly 쿠키 저장 시 XSS 공격은 막을 수 있지만, CSRF 공격에 취약함
 * -> 백엔드 서버로 재요청하여 헤더에 담아서 보냄. 프론트엔드는 로컬스토리지에 액세스 토큰 저장
 */
@RestController // 이 클래스가 REST 컨트롤러임을 나타냄 (JSON 응답을 반환)
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class OAuth2Controller {

    private final OAuth2JwtHeaderService oAuth2JwtHeaderService; // OAuth2 JWT 관련 서비스

    @PostMapping("/oauth2-jwt-header") // HTTP POST 요청을 "/oauth2-jwt-header" 경로에서 처리
    public String oauth2JwtHeader(HttpServletRequest request, HttpServletResponse response) {
        return oAuth2JwtHeaderService.oauth2JwtHeaderSet(request, response); // JWT를 헤더에 설정하는 서비스 호출
    }

    // ⚠️ OAuth2 로그인 후 JWT 토큰을 쿠키에 저장하는 방식
    // ⚠️ XSS 공격 방지는 가능하지만 CSRF 공격에 취약하므로 추가적인 보안 조치 필요
    // ⚠️ 프론트엔드에서 쿠키 대신 로컬 스토리지에 액세스 토큰을 저장하는 방식 사용
}
