package com.example.backend.security.constant;

public class TokenConstants {
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 30 * 60 * 1000L; // 30분 (밀리초)
    public static final String ACCESS_TOKEN_CATEGORY = "access_token";

    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 24시간 (밀리초)
    public static final String REFRESH_TOKEN_CATEGORY = "refresh_token";
    public static final long REFRESH_TOKEN_REDIS_TTL = 24 * 60 * 60; // 24시간 (초)

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public static final String REFRESH_TOKEN_REDIS_PREFIX = "refreshToken:";

    public static final String TOKEN_CLAIM_CATEGORY = "category";
    public static final String TOKEN_CLAIM_USERNAME = "username";
    public static final String TOKEN_CLAIM_EMAIL = "email";
    public static final String TOKEN_CLAIM_ROLE = "role";

    public static final String TOKEN_REISSUE_PATH = "/reissue";

    public static final String TOKEN_EXPIRED_MESSAGE = "JWT Token Expired";
    public static final String TOKEN_INVALID_MESSAGE = "Invalid JWT Token";
    public static final String TOKEN_NULL_MESSAGE = "JWT Token is null";
}
