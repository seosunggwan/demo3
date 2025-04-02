package com.example.backend.security.controller;

import com.example.backend.security.dto.UserProfileDto;
import com.example.backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    
    /**
     * 프로필 이미지 업로드를 위한 Presigned URL 요청
     */
    public record PresignedUrlRequest(String filename) {}
    
    @PostMapping("/profile/image/presigned")
    public ResponseEntity<?> getProfileImagePresignedUrl(@RequestBody PresignedUrlRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        try {
            log.info("프로필 이미지 Presigned URL 요청 - 사용자: {}, 파일명: {}", email, request.filename());
            
            // 파일명으로부터 고유 키 생성 (UUID)
            String originalFilename = request.filename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileKey = "profiles/" + UUID.randomUUID() + fileExtension;
            
            // S3 Presigned URL 생성 요청
            Map<String, String> presignedData = userService.generateProfileImagePresignedUrl(fileKey);
            
            log.info("Presigned URL 생성 완료 - 사용자: {}, 키: {}", email, fileKey);
            return ResponseEntity.ok(presignedData);
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - 파일명: {}, 오류: {}", request.filename(), e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미지 업로드 URL 생성에 실패했습니다: " + e.getMessage());
            
            // 개발 환경에서는 디버깅 정보 추가
            response.put("debug", "cloud.aws.s3.bucket 설정이 누락되었거나 S3 관련 의존성 주입에 문제가 있을 수 있습니다.");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 이미지 삭제 요청을 위한 레코드
     */
    public record DeleteImageRequest(String imageUrl) {}

    /**
     * S3에서 프로필 이미지를 삭제
     * @param request 삭제할 이미지 URL이 포함된 요청 객체
     * @return 삭제 결과
     */
    @DeleteMapping("/profile/image")
    public ResponseEntity<?> deleteProfileImage(@RequestBody DeleteImageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        try {
            log.info("프로필 이미지 삭제 요청 - 사용자: {}, URL: {}", email, request.imageUrl());
            
            // 이미지 URL 유효성 검사
            if (request.imageUrl() == null || request.imageUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "삭제할 이미지 URL이 제공되지 않았습니다."));
            }
            
            // 현재 사용자 프로필 정보 조회
            UserProfileDto userProfile = userService.getUserProfile(email);
            
            // 현재 사용자의 프로필 이미지가 맞는지 확인
            if (userProfile.getProfileImageUrl() == null || !userProfile.getProfileImageUrl().equals(request.imageUrl())) {
                log.warn("프로필 이미지 삭제 권한 없음 - 사용자: {}, 요청 URL: {}, 프로필 URL: {}", 
                         email, request.imageUrl(), userProfile.getProfileImageUrl());
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "이 이미지를 삭제할 권한이 없습니다."));
            }
            
            // S3에서 이미지 삭제
            userService.deleteProfileImageFromS3(request.imageUrl());
            
            // 사용자 프로필에서 이미지 URL 제거
            UserProfileDto profileDto = new UserProfileDto();
            profileDto.setProfileImageUrl(null);
            userService.updateUserProfile(email, profileDto);
            
            log.info("프로필 이미지 삭제 완료 - 사용자: {}, URL: {}", email, request.imageUrl());
            return ResponseEntity.ok(Map.of("success", true, "message", "프로필 이미지가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 중 오류 발생 - 사용자: {}, URL: {}, 오류: {}", 
                     email, request.imageUrl(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
} 