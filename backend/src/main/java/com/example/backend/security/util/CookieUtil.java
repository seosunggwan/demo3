package com.example.backend.security.util;

import jakarta.servlet.http.Cookie;

/**
 * 📌 쿠키 관련 유틸리티 클래스
 * - JWT 기반 인증에서 Refresh Token을 httpOnly 쿠키로 저장하기 위한 메서드 제공
 * - 쿠키를 생성할 때 보안 설정을 적용 (httpOnly, path 설정)
 */
public class CookieUtil {

    /**
     * 🔹 새로운 쿠키를 생성하는 메서드
     * - key: 쿠키 이름
     * - value: 쿠키 값 (JWT 또는 기타 데이터)
     * - expiredS: 만료 시간(초 단위)
     */
    public static Cookie createCookie(String key, String value, Integer expiredS) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true); // 🔹 XSS 공격 방지를 위해 httpOnly 설정 (JavaScript에서 접근 불가)
        cookie.setPath("/"); // 🔹 쿠키가 모든 경로에서 유효하도록 설정
        cookie.setMaxAge(expiredS); // 🔹 쿠키 만료 시간 설정 (초 단위)
        return cookie;
    }
}
