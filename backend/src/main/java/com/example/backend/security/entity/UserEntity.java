package com.example.backend.security.entity;

import com.example.backend.common.domain.BaseTimeEntity;
import com.example.backend.order.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 📌 일반 사용자 정보를 저장하는 엔터티
 * - 회원가입한 사용자의 계정 정보를 저장하는 테이블
 * - Spring Security에서 사용자 인증을 위한 주요 엔티티
 */
@Entity // JPA 엔티티 선언 (DB 테이블로 매핑됨)
@Builder // 빌더 패턴 지원 (객체 생성 편리)
@AllArgsConstructor // 모든 필드 포함된 생성자 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@Getter // 모든 필드에 대한 getter 자동 생성
public class UserEntity extends BaseTimeEntity {

    @Id // 기본키 (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 자동 증가(AUTO_INCREMENT)
    private Long id;

    private String username; // 사용자 이름

    @Column(nullable = false, unique = true) // NotNull + 유니크 제약 조건 추가
    private String email; // 로그인 ID 역할

    private String password; // 비밀번호 (암호화되어 저장됨)

    @Enumerated(EnumType.STRING) // Enum 값을 String으로 저장
    @Builder.Default // 빌더 패턴 사용 시 기본값 설정
    private Role role = Role.USER; // 기본값: 일반 사용자 (USER)

    @Embedded
    private Address address;
    
    private String profileImageUrl; // 프로필 이미지 URL

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
    
    /**
     * 사용자 이름을 업데이트합니다.
     * 
     * @param username 새로운 사용자 이름
     */
    public void updateUsername(String username) {
        this.username = username;
    }
    
    /**
     * 주소 정보를 업데이트합니다.
     * 
     * @param address 새로운 주소 정보
     */
    public void updateAddress(Address address) {
        this.address = address;
    }
    
    /**
     * 프로필 이미지 URL을 업데이트합니다.
     * 
     * @param profileImageUrl 새로운 프로필 이미지 URL
     */
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}