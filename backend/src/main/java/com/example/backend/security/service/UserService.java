package com.example.backend.security.service;

import com.example.backend.security.dto.UserProfileDto;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

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
                .profileImageUrl(user.getProfileImageUrl())
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
        
        // 프로필 이미지 URL 업데이트 (필요한 경우)
        if (profileDto.getProfileImageUrl() != null) {
            user.updateProfileImageUrl(profileDto.getProfileImageUrl());
        }
        
        userRepository.save(user);
        
        // 업데이트된 정보로 DTO 생성
        UserProfileDto updatedProfile = UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
        
        // 주소 정보 설정
        updatedProfile.setAddressInfo(user.getAddress());
        
        return updatedProfile;
    }
    
    /**
     * 프로필 이미지 업로드를 위한 Presigned URL을 생성합니다.
     * 
     * @param fileKey S3에 저장될 파일 키(경로)
     * @return Presigned URL 정보 (presignedUrl, fileUrl)
     */
    public Map<String, String> generateProfileImagePresignedUrl(String fileKey) {
        try {
            log.info("프로필 이미지 Presigned URL 생성 - 파일 키: {}", fileKey);
            
            // 파일 확장자로부터 Content-Type 결정
            String contentType = determineContentType(fileKey);
            
            // 10분 후 만료
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += TimeUnit.MINUTES.toMillis(10);
            expiration.setTime(expTimeMillis);
            
            // Presigned URL 생성 요청
            GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                    new GeneratePresignedUrlRequest(bucketName, fileKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);
            
            // URL 쿼리 파라미터에 Content-Type 추가 (Presigned URL에 포함)
            generatePresignedUrlRequest.addRequestParameter(
                "Content-Type", 
                contentType
            );
            
            // Presigned URL 생성
            URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            String fileUrl = getFileUrl(fileKey);
            
            log.info("Presigned URL 생성됨 - 키: {}, Content-Type: {}", fileKey, contentType);
            
            Map<String, String> result = new HashMap<>();
            result.put("presignedUrl", presignedUrl.toString());
            result.put("fileUrl", fileUrl);
            
            return result;
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 URL 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * S3에서 프로필 이미지를 삭제합니다.
     * 
     * @param imageUrl 삭제할 이미지 URL
     */
    public void deleteProfileImageFromS3(String imageUrl) {
        try {
            String fileKey = extractFileKeyFromUrl(imageUrl);
            
            if (fileKey == null) {
                log.warn("이미지 URL에서 파일 키를 추출할 수 없음: {}", imageUrl);
                throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
            }
            
            log.info("S3에서 이미지 삭제 - 키: {}", fileKey);
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
            log.info("S3에서 이미지 삭제 완료 - 키: {}", fileKey);
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 이미지 URL에서 파일 키를 추출합니다.
     */
    private String extractFileKeyFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return null;
            }
            
            // 모의 URL 체크 및 처리
            if (imageUrl.contains("example.com/mock-presigned-url")) {
                // 파일 이름 추출
                String[] parts = imageUrl.split("mock-presigned-url/");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            
            // S3 URL 형식 확인 (https://버킷명.s3.리전.amazonaws.com/파일키)
            if (imageUrl.contains(bucketName + ".s3.")) {
                // URL에서 버킷명 이후의 경로 추출
                String[] parts = imageUrl.split(bucketName + ".s3.[^/]+/");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            
            // URL에 쿼리 파라미터가 있는 경우 제거
            if (imageUrl.contains("?")) {
                imageUrl = imageUrl.substring(0, imageUrl.indexOf("?"));
            }
            
            // 마지막 슬래시 이후의 문자열을 파일명으로 간주
            int lastSlashIndex = imageUrl.lastIndexOf("/");
            if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
                String fileName = imageUrl.substring(lastSlashIndex + 1);
                return "profiles/" + fileName;
            }
            
            return null;
        } catch (Exception e) {
            log.error("이미지 URL에서 파일 키 추출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 파일 확장자에 따른 Content-Type을 결정합니다.
     */
    private String determineContentType(String fileKey) {
        String lowercaseKey = fileKey.toLowerCase();
        
        if (lowercaseKey.endsWith(".jpg") || lowercaseKey.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseKey.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseKey.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseKey.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowercaseKey.endsWith(".webp")) {
            return "image/webp";
        } else if (lowercaseKey.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            // 기본값
            return "application/octet-stream";
        }
    }
    
    /**
     * 파일 URL을 반환합니다.
     */
    private String getFileUrl(String fileKey) {
        return amazonS3.getUrl(bucketName, fileKey).toString();
    }
} 