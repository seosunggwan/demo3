package com.example.backend.security.repository;

import com.example.backend.security.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * 📌 일반 사용자 정보를 관리하는 JPA Repository
 * - UserEntity와 연결되어 DB에서 사용자 정보를 조회 및 저장하는 역할
 * - 회원가입 시 사용자 중복 여부 확인 가능
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 🔹 특정 email이 존재하는지 확인
     * - 회원가입 시 중복 확인 용도로 사용
     */
    boolean existsByEmail(String email);

    /**
     * 🔹 email을 기준으로 사용자 정보 조회
     * - 로그인 시 **email**을 기반으로 사용자를 찾을 때 사용
     */
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    
    // 페이지네이션과 검색을 위한 메소드 추가
    @NonNull
    Page<UserEntity> findAll(@NonNull Pageable pageable);
    
    // 이름으로 검색 (contains)
    Page<UserEntity> findByUsernameContaining(String username, Pageable pageable);
}
