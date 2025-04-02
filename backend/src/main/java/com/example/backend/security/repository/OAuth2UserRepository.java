package com.example.backend.security.repository;

import com.example.backend.security.entity.OAuth2UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 📌 OAuth2 사용자 정보를 관리하는 JPA Repository
 * - OAuth2UserEntity와 연결되어 DB에서 사용자 정보를 조회 및 저장하는 역할
 * - OAuth2 로그인 사용자의 정보를 username 기준으로 조회 가능
 */
public interface OAuth2UserRepository extends JpaRepository<OAuth2UserEntity, Long> {

    /**
     * 🔹 OAuth2 사용자의 username으로 정보 조회
     * - username 형식: provider + providerId (ex: google_12345, github_67890)
     * - OAuth2 로그인 시 기존 사용자가 존재하는지 확인하는 데 사용
     */
    OAuth2UserEntity findByUsername(String username);
}
