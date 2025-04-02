package com.example.backend.security.dto.oauth2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 📌 GitHub OAuth2 로그인 응답을 처리하는 클래스
 * - OAuth2Response 인터페이스를 구현하여 GitHub 사용자 정보 제공
 * - GitHub에서 받은 사용자 데이터를 attribute(Map)으로 저장하여 관리
 */
@RequiredArgsConstructor // Lombok을 사용하여 final 필드에 대한 생성자 자동 생성
public class GithubResponse implements OAuth2Response {

    private final Map<String, Object> attribute; // GitHub에서 받은 사용자 정보

    @Override
    public String getProvider() {
        return "github"; // 🔹 OAuth2 제공자(GitHub) 이름 반환
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString(); // 🔹 GitHub 사용자의 고유 ID 반환
    }

    @Override
    public String getName() {
        return attribute.get("name").toString(); // 🔹 사용자의 이름 반환
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString(); // 🔹 사용자의 이메일 반환
    }

    // ⚠️ GitHub의 OAuth2 응답에서 'email' 값이 null일 수 있음
    // ⚠️ 이메일이 공개되지 않은 경우 추가 API 호출 필요
}
