package com.example.backend.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 📌 OAuth2 사용자 정보를 저장하는 엔터티
 * - OAuth2 로그인한 사용자의 정보를 DB에 저장하는 역할
 * - username 필드는 "provider + providerId" 형식으로 저장 (ex: "google_123456789")
 */
@NoArgsConstructor // 🔹 기본 생성자 자동 생성 (JPA 필수)
@Entity // 🔹 JPA 엔티티 지정 (DB 테이블과 매핑)
@Getter @Setter // 🔹 getter, setter 자동 생성 (Lombok)
public class OAuth2UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 🔹 기본 키 자동 생성 (AUTO_INCREMENT)
    private Long id;

    private String username; // 🔹 OAuth2 제공자 + 사용자 ID (ex: google_12345, github_67890)
    private String name; // 🔹 사용자의 실제 이름
    private String email; // 🔹 사용자의 이메일
    private String role; // 🔹 사용자 역할 (ex: ROLE_USER, ROLE_ADMIN)

    @Builder // 🔹 빌더 패턴을 사용하여 객체 생성
    public OAuth2UserEntity(String username, String email, String name, String role) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // ⚠️ OAuth2 사용자 정보는 이메일이 null일 수도 있음 (GitHub의 경우 이메일 비공개 가능)
    // ⚠️ provider + providerId 형식으로 username을 저장하여 중복 방지
}
