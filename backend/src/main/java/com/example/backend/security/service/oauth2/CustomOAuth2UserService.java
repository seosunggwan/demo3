package com.example.backend.security.service.oauth2;

import com.example.backend.security.dto.oauth2.*;
import com.example.backend.security.entity.OAuth2UserEntity;
import com.example.backend.security.repository.OAuth2UserRepository;
import com.example.backend.security.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.logging.Logger;

/**
 * 📌 OAuth2 사용자 정보를 가져오는 서비스
 * - OAuth2 로그인 시 사용자 정보를 조회하고 저장하는 역할
 * - 구글, 네이버, 깃허브 등의 OAuth2 제공자별로 응답 데이터를 처리
 */
@Service
@RequiredArgsConstructor // 🔹 Lombok을 사용하여 생성자 주입 자동화
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = Logger.getLogger(CustomOAuth2UserService.class.getName());
    private final OAuth2UserRepository oAuth2UserRepository; // 🔹 OAuth2 사용자 정보를 관리하는 Repository

    /**
     * 🔹 OAuth2 로그인 시 호출되는 메서드
     * - OAuth2UserRequest를 기반으로 사용자 정보를 가져와 처리
     * - OAuth2 제공자에 따라 적절한 DTO로 변환 후 저장
     */
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 🔹 super.loadUser()를 호출하여 OAuth2 제공자로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String clientName = userRequest.getClientRegistration().getClientName();
        
        logger.info("=== OAuth2 로그인 요청 처리 시작 ===");
        logger.info("OAuth2 제공자: " + clientName);
        logger.info("원본 OAuth2 속성: " + oAuth2User.getAttributes());

        OAuth2Response response = null;
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 🔹 OAuth2 제공자별 응답 객체 생성 (네이버, 구글, 깃허브)
        if (clientName.equals("naver")) {
            logger.info("네이버 로그인 응답 처리");
            response = new NaverResponse(attributes);
        } else if (clientName.equals("google")) {
            logger.info("구글 로그인 응답 처리");
            response = new GoogleResponse(attributes);
        } else if (clientName.equals("github")) {
            logger.info("깃허브 로그인 응답 처리");
            response = new GithubResponse(attributes);
        } else {
            logger.warning("지원되지 않는 OAuth2 제공자: " + clientName);
            return null; // 🔹 지원되지 않는 OAuth2 제공자인 경우 null 반환
        }

        // 🔹 provider + providerId 조합으로 username 생성 (ex: "google_12345")
        String username = response.getProvider() + " " + response.getProviderId();
        String role = Role.USER.name(); // 일반 사용자 권한으로 설정 (USER)
        
        logger.info("변환된 OAuth2 사용자 정보:");
        logger.info("제공자: " + response.getProvider());
        logger.info("ID: " + response.getProviderId());
        logger.info("이름: " + response.getName());
        logger.info("이메일: " + response.getEmail());
        logger.info("통합 username: " + username);

        // 🔹 사용자 정보를 데이터베이스에 저장 (신규 사용자는 추가, 기존 사용자는 업데이트)
        saveUser(response, username, role);

        // 🔹 Entity를 DTO로 변환하여 OAuth2 인증된 사용자 객체 생성
        OAuth2UserDto oAuth2UserDto = OAuth2UserDto.builder()
                .username(username)
                .name(response.getName())
                .email(response.getEmail())
                .role(role)
                .build();
                
        logger.info("OAuth2 인증 완료: " + username);
        logger.info("=== OAuth2 로그인 요청 처리 완료 ===");

        return new CustomOAuth2User(oAuth2UserDto); // 🔹 인증된 사용자 정보 반환
    }

    /**
     * 🔹 OAuth2 사용자 정보를 저장 또는 업데이트하는 메서드
     * - 사용자가 이미 존재하면 정보 업데이트
     * - 존재하지 않으면 새로운 사용자 데이터 저장
     */
    private void saveUser(OAuth2Response response, String username, String role) {
        // 🔹 데이터베이스에서 해당 username을 가진 사용자 조회
        OAuth2UserEntity isExist = oAuth2UserRepository.findByUsername(username);
        
        logger.info("=== OAuth2 사용자 정보 저장 ===");
        logger.info("Username: " + username);
        logger.info("이메일: " + response.getEmail());

        if (isExist != null) {
            // 🔹 기존 사용자가 존재하면 정보 업데이트
            logger.info("기존 OAuth2 사용자 정보 업데이트");
            isExist.setName(response.getName());
            isExist.setEmail(response.getEmail());
            isExist.setRole(role);
            logger.info("사용자 ID: " + isExist.getId() + " 정보 업데이트 완료");
        } else {
            // 🔹 새로운 사용자 정보를 데이터베이스에 저장
            logger.info("새로운 OAuth2 사용자 저장");
            OAuth2UserEntity oAuth2UserEntity = OAuth2UserEntity.builder()
                    .username(username)
                    .name(response.getName())
                    .email(response.getEmail())
                    .role(role)
                    .build();
            OAuth2UserEntity saved = oAuth2UserRepository.save(oAuth2UserEntity);
            logger.info("신규 사용자 저장 완료, ID: " + saved.getId());
        }
        
        logger.info("=== OAuth2 사용자 정보 저장 완료 ===");
    }
}
