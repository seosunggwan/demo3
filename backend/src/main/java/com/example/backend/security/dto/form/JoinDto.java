package com.example.backend.security.dto.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 📌 회원가입 요청을 처리하기 위한 DTO (Data Transfer Object)
 * - 클라이언트에서 전송한 회원가입 데이터를 담는 객체
 * - @Getter, @Setter 사용으로 getter/setter 자동 생성
 * - @NoArgsConstructor 사용으로 기본 생성자 자동 생성
 */
@NoArgsConstructor // 🔹 기본 생성자 자동 생성 (Lombok)
@Getter @Setter // 🔹 getter, setter 자동 생성 (Lombok)
public class JoinDto {

    private String email; // 🔹 사용자가 입력한 email
    private String username; // 🔹 사용자가 입력한 아이디
    private String password; // 🔹 사용자가 입력한 비밀번호

    private String city;        // 도시
    private String street;      // 거리
    private String zipcode;     // 우편번호

    // 🔹 생성자 (직접 값 할당 가능)
    public JoinDto(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // ⚠️ 비밀번호는 반드시 암호화하여 저장해야 함 (서비스 계층에서 처리)
    // ⚠️ 추가로 이메일, 닉네임, 전화번호 등의 필드를 추가할 수 있음
}
