package com.example.backend.security.dto.oauth2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 📌 Google OAuth2 로그인 응답을 처리하는 클래스
 * - OAuth2Response 인터페이스를 구현하여 Google 사용자 정보 제공
 * - Google에서 받은 사용자 데이터를 attribute(Map)으로 저장하여 관리
 */
@RequiredArgsConstructor // Lombok을 사용하여 final 필드에 대한 생성자 자동 생성
public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attribute; // Google에서 받은 사용자 정보

    @Override
    public String getProvider() {
        return "google"; // 🔹 OAuth2 제공자(Google) 이름 반환
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString(); // 🔹 Google 사용자의 고유 ID 반환 (sub 필드 사용)
    }

    @Override
    public String getName() {
        return attribute.get("name").toString(); // 🔹 사용자의 이름 반환
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString(); // 🔹 사용자의 이메일 반환
    }

    // ⚠️ Google OAuth2 응답 구조에 따라 추가 정보(ex. 프로필 사진) 활용 가능
    // ⚠️ Google OAuth2 응답 필드는 공식 문서를 참고하여 업데이트 필요
}
