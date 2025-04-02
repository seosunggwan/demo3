package com.example.backend.security.dto.oauth2;

import java.util.Map;
import java.util.logging.Logger;

/**
 * 📌 Naver OAuth2 로그인 응답을 처리하는 클래스
 * - OAuth2Response 인터페이스를 구현하여 Naver 사용자 정보 제공
 * - Naver에서 받은 사용자 데이터를 attribute(Map)으로 저장하여 관리
 */
public class NaverResponse implements OAuth2Response {

    private static final Logger logger = Logger.getLogger(NaverResponse.class.getName());
    private final Map<String, Object> attribute; // Naver에서 받은 사용자 정보
    private final Map<String, Object> originalAttribute; // 원본 응답 저장

    /**
     * 🔹 Naver OAuth2 응답은 "response" 필드 안에 사용자 정보가 존재함
     * - 따라서 생성자에서 "response" 키를 사용하여 attribute에 저장
     */
    public NaverResponse(Map<String, Object> attribute) {
        this.originalAttribute = attribute;
        
        // 디버깅: 전체 응답 구조 로깅
        logger.info("=== 네이버 OAuth2 원본 응답 ===");
        logger.info(attribute.toString());
        
        // response 키가 존재하는지 확인
        if (attribute.containsKey("response")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) attribute.get("response");
            this.attribute = responseMap;
            
            // 디버깅: response 내부 구조 로깅
            logger.info("=== 네이버 response 객체 내용 ===");
            logger.info(this.attribute.toString());
        } else {
            // response 키가 없는 경우 원본 데이터 사용
            logger.warning("네이버 응답에 'response' 키가 없습니다. 원본 데이터 사용");
            this.attribute = attribute;
        }
    }

    @Override
    public String getProvider() {
        return "naver"; // 🔹 OAuth2 제공자(Naver) 이름 반환
    }

    @Override
    public String getProviderId() {
        try {
            return attribute.get("id").toString(); // 🔹 Naver 사용자의 고유 ID 반환
        } catch (Exception e) {
            logger.severe("네이버 ID 정보를 가져오는 중 오류 발생: " + e.getMessage());
            return "unknown";
        }
    }

    @Override
    public String getName() {
        try {
            return attribute.get("name").toString(); // 🔹 사용자의 이름 반환
        } catch (Exception e) {
            logger.severe("네이버 이름 정보를 가져오는 중 오류 발생: " + e.getMessage());
            return "Unknown User";
        }
    }

    @Override
    public String getEmail() {
        try {
            return attribute.get("email").toString(); // 🔹 사용자의 이메일 반환
        } catch (Exception e) {
            logger.severe("네이버 이메일 정보를 가져오는 중 오류 발생: " + e.getMessage());
            return "";
        }
    }

    // 원본 응답 정보를 문자열로 반환
    public String getOriginalResponse() {
        return originalAttribute.toString();
    }

    // ⚠️ Naver OAuth2 응답은 "response" 필드 안에 사용자 정보가 포함됨
    // ⚠️ OAuth2 표준 응답과 구조가 다르므로 별도 처리 필요
}
