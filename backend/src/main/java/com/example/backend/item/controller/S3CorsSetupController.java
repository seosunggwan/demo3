package com.example.backend.item.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * S3 버킷 CORS 설정을 수동으로 적용하기 위한 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3CorsSetupController {

    private final AmazonS3 amazonS3;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    
    /**
     * S3 버킷의 현재 CORS 설정을 확인
     * @return 현재 CORS 설정 정보
     */
    @GetMapping("/cors-status")
    public ResponseEntity<Map<String, Object>> getCorsStatus() {
        try {
            BucketCrossOriginConfiguration corsConfig = amazonS3.getBucketCrossOriginConfiguration(bucketName);
            
            if (corsConfig == null || corsConfig.getRules() == null || corsConfig.getRules().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "not_configured",
                    "message", "S3 버킷에 CORS 설정이 되어있지 않습니다."
                ));
            }
            
            List<Map<String, Object>> rules = corsConfig.getRules().stream()
                .map(rule -> Map.of(
                    "allowedOrigins", rule.getAllowedOrigins(),
                    "allowedMethods", rule.getAllowedMethods(),
                    "allowedHeaders", rule.getAllowedHeaders(),
                    "exposedHeaders", rule.getExposedHeaders(),
                    "maxAgeSeconds", rule.getMaxAgeSeconds()
                ))
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "status", "configured",
                "message", "CORS 설정이 적용되어 있습니다.",
                "rules", rules
            ));
        } catch (Exception e) {
            log.error("CORS 설정 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "CORS 설정 상태 확인 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * S3 버킷에 CORS 설정을 수동으로 적용
     * @return 설정 결과
     */
    @PostMapping("/setup-cors")
    public ResponseEntity<Map<String, String>> setupCorsConfiguration() {
        try {
            // CORS 규칙 생성 (매우 관대한 설정)
            List<CORSRule> rules = Arrays.asList(
                new CORSRule()
                    .withAllowedMethods(Arrays.asList(
                        CORSRule.AllowedMethods.GET, 
                        CORSRule.AllowedMethods.PUT, 
                        CORSRule.AllowedMethods.POST, 
                        CORSRule.AllowedMethods.DELETE, 
                        CORSRule.AllowedMethods.HEAD
                    ))
                    .withAllowedOrigins(Arrays.asList(
                        "http://localhost:5173", 
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "https://your-production-domain.com",
                        "*" // 모든 도메인 허용 (개발 환경에서만 사용)
                    ))
                    .withAllowedHeaders(Arrays.asList("*"))
                    .withMaxAgeSeconds(3600)
                    .withExposedHeaders(Arrays.asList(
                        "ETag", 
                        "Content-Type", 
                        "Content-Length", 
                        "Accept", 
                        "Authorization", 
                        "x-amz-*"
                    ))
            );
            
            // CORS 설정 생성 및 버킷에 적용
            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration().withRules(rules);
            amazonS3.setBucketCrossOriginConfiguration(bucketName, configuration);
            
            log.info("S3 버킷 CORS 설정 완료: {}", bucketName);
            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "message", "S3 버킷 CORS 설정이 성공적으로 적용되었습니다."
            ));
        } catch (Exception e) {
            log.error("S3 버킷 CORS 설정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error", 
                    "message", "S3 버킷 CORS 설정 중 오류가 발생했습니다: " + e.getMessage()
                ));
        }
    }
} 