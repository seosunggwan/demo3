package com.example.backend.common.config.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * AWS S3 연동을 위한 설정 클래스
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3Config {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    
    // 생성된 amazonS3 빈을 관리하기 위한 필드
    private AmazonS3 amazonS3Client;

    /**
     * AmazonS3 클라이언트 빈 생성
     */
    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        amazonS3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
        return amazonS3Client;
    }
    
    /**
     * RestTemplate 빈 생성 (필요시 사용)
     */
    @Bean(name = "s3RestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * S3 버킷에 CORS 설정 적용
     * 애플리케이션 시작 시 자동으로 실행됨
     */
    @PostConstruct
    public void configureBucketCors() {
        // 빈이 완전히 초기화된 후 실행
        // amazonS3() 메소드 직접 호출 대신 Spring이 초기화한 빈 사용
        if (amazonS3Client == null) {
            log.warn("AmazonS3 클라이언트가 아직 초기화되지 않았습니다.");
            return;
        }
        
        try {
            // CORS 규칙 생성
            List<CORSRule> rules = Arrays.asList(
                new CORSRule()
                    .withAllowedMethods(Arrays.asList(
                        CORSRule.AllowedMethods.GET, 
                        CORSRule.AllowedMethods.PUT, 
                        CORSRule.AllowedMethods.POST, 
                        CORSRule.AllowedMethods.DELETE, 
                        CORSRule.AllowedMethods.HEAD
                    ))
                    .withAllowedOrigins(Arrays.asList("http://localhost:5173", "https://your-production-domain.com"))
                    .withAllowedHeaders(Arrays.asList("*"))
                    .withMaxAgeSeconds(3000)
                    .withExposedHeaders(Arrays.asList("ETag", "x-amz-meta-custom-header"))
            );
            
            // CORS 설정 생성 및 버킷에 적용
            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration().withRules(rules);
            amazonS3Client.setBucketCrossOriginConfiguration(bucketName, configuration);
            
            log.info("S3 버킷 CORS 설정 완료: {}", bucketName);
        } catch (Exception e) {
            log.error("S3 버킷 CORS 설정 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 