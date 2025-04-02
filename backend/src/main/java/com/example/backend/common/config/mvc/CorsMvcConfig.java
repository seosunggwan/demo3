package com.example.backend.common.config.mvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냄
public class CorsMvcConfig implements WebMvcConfigurer { // Spring MVC 설정을 위해 WebMvcConfigurer 구현

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 정책 적용
                .allowedOrigins("http://localhost:5173") // 프론트엔드 URL 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // DELETE 허용
                .allowedHeaders("*") // 모든 헤더 허용
                
                .exposedHeaders("Set-Cookie", "Authorization", "access_token") // 클라이언트가 'Set-Cookie'와 'Authorization' 헤더를 읽을 수 있도록 허용
                
                .allowCredentials(true); // 인증 정보 포함 허용
    }
    
    /**
     * 메시지 컨버터 설정 - 한글 인코딩 처리
     */
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setWriteAcceptCharset(false);
        converters.add(0, converter);
        WebMvcConfigurer.super.configureMessageConverters(converters);
    }
    
    /**
     * 문자 인코딩 필터 설정 - 한글 인코딩 처리
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }
}
