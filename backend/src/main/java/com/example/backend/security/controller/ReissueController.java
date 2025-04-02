package com.example.backend.security.controller;

import com.example.backend.security.service.ReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refresh 토큰을 이용한 액세스 토큰 재발급 요청 처리
 * Refresh Token Rotation 적용 (재사용 공격 방지)
 */
@RestController // 이 클래스가 REST 컨트롤러임을 나타냄 (JSON 응답을 반환)
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class ReissueController {

    private final ReissueService reissueService; // 토큰 재발급 관련 서비스

    @PostMapping("/reissue") // HTTP POST 요청을 "/reissue" 경로에서 처리
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return reissueService.reissue(request, response); // 요청을 받아 ReissueService에서 토큰 재발급 처리
    }

    // ⚠️ Refresh Token Rotation 적용: 새로운 리프레시 토큰을 발급하며 기존 토큰 폐기
    // ⚠️ 요청 시 쿠키 또는 헤더에서 Refresh Token을 가져와 검증 후 새로운 Access Token 반환
    // ⚠️ 만료된 리프레시 토큰으로 요청 시 401 Unauthorized 응답
}
