package com.example.backend.security.entity;

// ✅ 회원 역할(권한) ENUM 정의
public enum Role {
    ADMIN, // 관리자 권한
    USER;  // 일반 사용자 권한 (기본값)

    // Spring Security를 위한 "ROLE_" 접두사가 붙은 권한 문자열 반환
    public String getAuthority() {
        return "ROLE_" + name();
    }
}

