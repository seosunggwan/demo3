package com.example.backend.security.dto.oauth2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 📌 OAuth2 로그인 후 사용자 정보를 담는 DTO
 * - OAuth2 제공자로부터 받은 정보를 표준화하여 저장
 * - `CustomOAuth2User`에서 사용됨
 */
@Getter @Setter // 🔹 Lombok을 사용하여 getter, setter 자동 생성
public class OAuth2UserDto {

    private String username; // 🔹 내부 시스템에서 사용할 사용자 ID
    private String name; // 🔹 OAuth2 제공자가 제공한 사용자 이름
    private String email; // 🔹 사용자 이메일
    private String role; // 🔹 사용자 역할 (ex: ROLE_USER, ROLE_ADMIN)

    @Builder // 🔹 빌더 패턴 지원 (객체 생성 시 가독성 향상)
    public OAuth2UserDto(String username, String name, String email, String role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // ⚠️ username은 내부 DB 식별자로 활용, 제공자 ID와 다를 수 있음
    // ⚠️ 필요하면 추가적인 사용자 정보 필드 확장 가능
}
