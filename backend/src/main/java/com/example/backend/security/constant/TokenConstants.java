package com.example.backend.security.constant;

/**
 * 📌 JWT 토큰 관련 상수 정의
 * - Access Token, Refresh Token의 유효시간 등 토큰 관련 상수들을 중앙화하여 관리
 */
public class TokenConstants {
    
    // 🔒 Access Token 관련 상수
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 60 * 10 * 1000L; // 10분 (밀리초)
    public static final String ACCESS_TOKEN_CATEGORY = "access_token";
    
    // 🔒 Refresh Token 관련 상수
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 24시간 (밀리초)
    public static final String REFRESH_TOKEN_CATEGORY = "refresh_token";
    public static final long REFRESH_TOKEN_REDIS_TTL = 24 * 60 * 60; // 24시간 (초)
    
    // 🔒 쿠키 관련 상수
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    
    // 🔒 Redis 관련 상수
    public static final String REFRESH_TOKEN_REDIS_PREFIX = "refreshToken:";
    
    // 🔒 토큰 클레임 관련 상수
    public static final String TOKEN_CLAIM_CATEGORY = "category";
    public static final String TOKEN_CLAIM_USERNAME = "username";
    public static final String TOKEN_CLAIM_EMAIL = "email";
    public static final String TOKEN_CLAIM_ROLE = "role";
    
    // 🔒 토큰 재발급 관련 상수
    public static final String TOKEN_REISSUE_PATH = "/reissue";
    
    // 🔒 토큰 검증 관련 상수
    public static final String TOKEN_EXPIRED_MESSAGE = "JWT Token Expired";
    public static final String TOKEN_INVALID_MESSAGE = "Invalid JWT Token";
    public static final String TOKEN_NULL_MESSAGE = "JWT Token is null";
} 