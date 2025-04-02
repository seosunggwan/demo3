package com.example.backend.security.repository;

import com.example.backend.security.entity.RefreshEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * 📌 Redis 기반 Refresh Token Repository
 * - Refresh Token을 Redis에 저장하고 관리
 */
@Repository
public interface RefreshRepository extends CrudRepository<RefreshEntity, String> {

    /**
     * 🔹 특정 Refresh Token 존재 여부 확인
     */
    boolean existsById(@NonNull String refresh);

    /**
     * 🔹 Refresh Token 삭제
     */
    void deleteById(@NonNull String refresh);
}