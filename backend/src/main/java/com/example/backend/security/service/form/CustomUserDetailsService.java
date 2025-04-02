package com.example.backend.security.service.form;

import com.example.backend.security.dto.form.CustomUserDetails;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 📌 Spring Security에서 사용자 정보를 가져오는 서비스
 * - UserRepository를 이용해 DB에서 사용자 정보를 조회하고 인증 처리
 * - 로그인 시 **email**만을 기반으로 사용자 정보를 로드하여 SecurityContext에 저장됨
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // 🔹 사용자 정보를 관리하는 JPA Repository

    /**
     * 🔹 사용자의 **email**을 기반으로 UserDetails를 반환
     * - 로그인 시 SecurityContext에서 호출됨
     * - UserRepository를 사용하여 DB에서 사용자 조회
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 로그인 시도 이메일: " + email); // ✅ 로그 추가
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
    }
}
