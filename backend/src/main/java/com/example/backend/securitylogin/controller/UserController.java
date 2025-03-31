package com.example.backend.securitylogin.controller;

import com.example.backend.securitylogin.dto.UserProfileDto;
import com.example.backend.securitylogin.entity.UserEntity;
import com.example.backend.securitylogin.repository.UserRepository;
import com.example.backend.securitylogin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        try {
            UserProfileDto userProfile = userService.getUserProfile(email);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            log.error("프로필 정보를 가져오는 중 오류 발생: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로필 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileDto profileDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        try {
            UserProfileDto updatedProfile = userService.updateUserProfile(email, profileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("프로필 정보를 업데이트하는 중 오류 발생: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로필 정보를 업데이트하는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 