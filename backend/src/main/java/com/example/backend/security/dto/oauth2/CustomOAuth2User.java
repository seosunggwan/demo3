package com.example.backend.security.dto.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 📌 OAuth2 사용자 정보를 담는 커스텀 클래스
 * - OAuth2User 인터페이스를 구현하여 사용자 정보 제공
 * - OAuth2 로그인 후 사용자 정보를 `OAuth2UserDto`에서 가져와 활용
 */
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserDto oAuth2UserDto; // OAuth2 사용자 정보를 저장하는 DTO

    // 🔹 OAuth2 로그인 제공자별 Attribute 정보 통일되지 않아 null 반환
    @Override
    public Map<String, Object> getAttributes() {
        return null; // 필요 시 특정 OAuth2 제공자의 정보 파싱 후 반환 가능
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 🔹 사용자의 권한(Role) 정보를 반환
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return oAuth2UserDto.getRole(); // 사용자의 역할 (ex: USER, ADMIN)
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return oAuth2UserDto.getName(); // 🔹 OAuth2 제공자가 제공하는 이름 반환
    }

    public String getUsername() {
        return oAuth2UserDto.getUsername(); // 🔹 내부 시스템에서 사용하는 사용자 ID 반환
    }

    public String getEmail() {
        return oAuth2UserDto.getEmail(); // 🔹 사용자의 이메일 정보 반환
    }

    // ⚠️ getAttributes()는 현재 null 반환, 필요하면 OAuth2 로그인 제공자별로 설정 가능
    // ⚠️ 추가적인 사용자 정보를 저장할 경우 OAuth2UserDto 확장 가능
}
