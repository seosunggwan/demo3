package com.example.backend.security.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스가 REST 컨트롤러임을 나타냄 (JSON 응답을 반환)
public class AdminController {

    @PostMapping("/admin") // HTTP POST 요청을 "/admin" 경로에서 처리
    public String adminP() {
        return "Admin Page"; // 단순한 문자열 응답 반환
    }

    // ⚠️ SecurityConfig에서 "/admin" 경로는 ADMIN 역할을 가진 사용자만 접근 가능
    // ⚠️ 만약 일반 사용자가 접근하려면 SecurityConfig에서 권한 설정 변경 필요
}