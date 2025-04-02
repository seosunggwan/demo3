package com.example.backend.security.service.oauth2;

import com.example.backend.security.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

/**
 * 📌 OAuth2 로그인 후 JWT Access Token을 헤더로 이동시키는 서비스
 * - OAuth2 로그인 시 Access Token이 httpOnly 쿠키로 발급됨
 * - 프론트엔드가 다시 요청하면 해당 쿠키에서 Access Token을 추출하여 헤더에 추가
 * - 쿠키에서 Access Token을 가져온 후 만료 처리 (보안 강화)
 */
@Service // 🔹 Spring의 Service 컴포넌트로 등록
public class OAuth2JwtHeaderService {

    /**
     * 🔹 OAuth2 로그인 후 Access Token을 헤더로 이동
     * - 클라이언트가 Access Token을 가져올 수 있도록 헤더에 추가
     * - 쿠키에 저장된 Access Token을 읽어오고, 쿠키를 만료시켜 보안 강화
     */
    public String oauth2JwtHeaderSet(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String access = null;

        // 🔹 쿠키가 존재하지 않으면 400 Bad Request 반환
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "bad";
        }

        // 🔹 쿠키 배열에서 "access_token" 토큰 값 찾기
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("access_token")) {
                access = cookie.getValue();
            }
        }

        // 🔹 Access Token이 없으면 400 Bad Request 반환
        if (access == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "bad";
        }

        // 🔹 Access Token을 헤더에 추가하여 프론트엔드가 사용할 수 있도록 함
        response.addHeader("access_token", access);

        // 🔹 클라이언트의 Access Token 쿠키를 만료시켜 보안 강화
        response.addCookie(CookieUtil.createCookie("access_token", null, 0));
        response.setStatus(HttpServletResponse.SC_OK);

        return "success";
    }
}
