package com.example.backend.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter // 모든 필드의 getter 자동 생성
@MappedSuperclass // JPA 엔티티가 공통적으로 상속받아 사용할 부모 클래스
public class BaseTimeEntity {

    @CreationTimestamp // 엔티티가 최초 생성될 때 자동으로 현재 시간 저장
    private LocalDateTime createdTime;

    @UpdateTimestamp // 엔티티가 수정될 때마다 자동으로 현재 시간 갱신
    private LocalDateTime updatedTime;
}
