package com.example.backend.security.service.oauth2;

import com.example.backend.security.entity.OAuth2UserEntity;
import com.example.backend.security.entity.Role;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.OAuth2UserRepository;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

/**
 * OAuth2 사용자와 일반 사용자 간의 매핑을 처리하는 서비스
 * - OAuth2 로그인 시 UserEntity도 함께 생성하여 채팅 서비스와의 호환성 보장
 */
@Service
@RequiredArgsConstructor
public class OAuthUserEntityToUserEntityService {

    private static final Logger logger = Logger.getLogger(OAuthUserEntityToUserEntityService.class.getName());
    private final OAuth2UserRepository oAuth2UserRepository;
    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자가 OAuth2 사용자인 경우 UserEntity로 변환 또는 찾아서 반환
     * - SecurityContext에서 현재 인증된 사용자의 이메일/이름을 가져옴
     * - 해당 이메일로 UserEntity를 찾거나 없으면 새로 생성
     */
    @Transactional
    public UserEntity getCurrentUserEntityFromOAuth() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // OAuth2 인증의 경우 provider id 형식 (예: "naver 12345")
        
        logger.info("현재 인증된 사용자: " + username);
        
        // 일반 로그인 사용자인 경우 바로 반환
        if (!username.contains(" ")) {
            logger.info("일반 로그인 사용자 확인됨: " + username);
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("인증된 사용자를 찾을 수 없습니다: " + username));
        }
        
        // OAuth2 사용자인 경우 처리
        logger.info("OAuth2 사용자 인증 확인됨: " + username);
        
        // OAuth2 사용자 정보 조회
        OAuth2UserEntity oAuth2User = oAuth2UserRepository.findByUsername(username);
        if (oAuth2User == null) {
            logger.severe("OAuth2 사용자 정보를 찾을 수 없음: " + username);
            throw new RuntimeException("OAuth2 인증된 사용자 정보를 찾을 수 없습니다: " + username);
        }
        
        // 이메일이 있는 경우 해당 이메일 사용
        String email = oAuth2User.getEmail();
        if (email != null && !email.isEmpty()) {
            logger.info("OAuth2 사용자의 실제 이메일 사용: " + email);
            return findOrCreateUserEntity(email, oAuth2User.getName());
        }
        
        // 이메일이 없는 경우에도 username을 그대로 사용 (가상 이메일 생성하지 않음)
        logger.info("OAuth2 사용자의 username을 이메일로 사용: " + username);
        return findOrCreateUserEntity(username, oAuth2User.getName());
    }
    
    /**
     * OAuth2UserEntity 객체로부터 UserEntity를 생성하거나 찾아서 반환
     */
    @Transactional
    public UserEntity syncOAuth2UserToUserEntity(String username, String name, String email) {
        logger.info("OAuth2 사용자 정보 동기화: " + username);
        
        // 이메일이 있는 경우
        if (email != null && !email.isEmpty()) {
            logger.info("실제 이메일로 사용자 동기화: " + email);
            return findOrCreateUserEntity(email, name);
        }
        
        // 이메일이 없는 경우 username을 그대로 사용 (가상 이메일 생성하지 않음)
        logger.info("사용자 ID를 이메일로 사용: " + username);
        return findOrCreateUserEntity(username, name);
    }
    
    /**
     * 이메일로 사용자를 찾거나 없으면 생성
     */
    @Transactional
    public UserEntity findOrCreateUserEntity(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    logger.info("새로운 UserEntity 생성: " + email);
                    UserEntity userEntity = UserEntity.builder()
                            .email(email)
                            .username(name)
                            .password("oauth2_user") // OAuth2 사용자는 비밀번호로 로그인하지 않음
                            .role(Role.USER)
                            .build();
                    return userRepository.save(userEntity);
                });
    }
} 