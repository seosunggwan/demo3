package com.example.backend.common.config.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 스케줄링 활성화를 위한 빈 설정 클래스
    // @Scheduled 애노테이션이 있는 메서드를 자동으로
    // 스케줄링 작업으로 등록하도록 활성화합니다.
} 