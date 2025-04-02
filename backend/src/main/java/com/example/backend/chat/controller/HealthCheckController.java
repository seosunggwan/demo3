package com.example.backend.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 서버 상태 확인을 위한 헬스 체크 컨트롤러
 */
@RestController
public class HealthCheckController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    /**
     * 서버 상태 확인 엔드포인트
     * @return 서버 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("헬스 체크 요청 수신: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Server is running");
        
        return ResponseEntity.ok(response);
    }
} 