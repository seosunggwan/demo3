package com.example.backend.securitylogin.service;

import com.example.backend.securitylogin.dto.UserProfileDto;
import com.example.backend.securitylogin.entity.UserEntity;
import com.example.backend.securitylogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 정보를 조회합니다.
     * 
     * @param email 사용자 이메일
     * @return 사용자 프로필 정보
     */
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        UserProfileDto profileDto = UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        
        // 주소 정보 설정
        profileDto.setAddressInfo(user.getAddress());
        
        return profileDto;
    }

    /**
     * 사용자 프로필 정보를 업데이트합니다.
     * 
     * @param email 사용자 이메일
     * @param profileDto 업데이트할 프로필 정보
     * @return 업데이트된 사용자 프로필 정보
     */
    @Transactional
    public UserProfileDto updateUserProfile(String email, UserProfileDto profileDto) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 사용자 이름 업데이트 (필요한 경우)
        if (profileDto.getUsername() != null && !profileDto.getUsername().isEmpty()) {
            user.updateUsername(profileDto.getUsername());
        }
        
        // 주소 정보 업데이트 (필요한 경우)
        if (profileDto.getCity() != null || profileDto.getStreet() != null || profileDto.getZipcode() != null) {
            user.updateAddress(profileDto.toAddress());
        }
        
        userRepository.save(user);
        
        // 업데이트된 정보로 DTO 생성
        UserProfileDto updatedProfile = UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        
        // 주소 정보 설정
        updatedProfile.setAddressInfo(user.getAddress());
        
        return updatedProfile;
    }
} 