package com.example.backend.security.dto.oauth2;

/**
 * 📌 OAuth2 사용자 정보를 표준화하기 위한 인터페이스
 * - 다양한 OAuth2 제공자(Google, GitHub, Naver 등)의 데이터를 통일된 형식으로 관리
 */
public interface OAuth2Response {

    public String getProvider(); // 🔹 OAuth2 제공자 이름 (google, github, naver 등)

    public String getProviderId(); // 🔹 제공자 내부의 사용자 ID

    public String getName(); // 🔹 제공자의 사용자 이름

    public String getEmail(); // 🔹 제공자의 사용자 이메일
}
