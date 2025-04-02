package com.example.backend.security.dto.form;

import com.example.backend.security.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 📌 Spring Security의 UserDetails 구현체
 * - 사용자 정보를 담고 있으며, 인증 및 권한 확인에 사용됨
 * - SecurityContext에서 인증된 사용자의 정보를 유지하는 역할
 */
public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity; // 사용자 엔터티 정보 저장

    // 생성자: UserEntity 객체를 받아와 저장
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 🔹 사용자의 권한을 반환 (Spring Security에서 사용)
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRole().name(); // DB에 저장된 역할 반환 (ex: USER, ADMIN)
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword(); // 🔹 Spring Security가 사용할 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail(); // 🔹 Spring Security가 사용할 사용자명 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 🔹 계정이 만료되지 않았는지 여부 (true: 활성 상태)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 🔹 계정이 잠겨있지 않은지 여부 (true: 활성 상태)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 🔹 비밀번호가 만료되지 않았는지 여부 (true: 활성 상태)
    }

    @Override
    public boolean isEnabled() {
        return true; // 🔹 계정이 활성화되어 있는지 여부 (true: 활성 상태)
    }

    // ⚠️ 위의 메서드들은 기본적으로 'true'를 반환하고 있는데,
    // ⚠️ 계정 상태(잠김, 비활성화, 만료 등)에 따라 다르게 설정할 수 있음
}
