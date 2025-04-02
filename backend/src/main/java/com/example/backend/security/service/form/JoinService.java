package com.example.backend.security.service.form;

import com.example.backend.security.dto.form.JoinDto;
import com.example.backend.security.entity.Address;
import com.example.backend.security.entity.Role;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 📌 회원가입 로직을 처리하는 서비스
 * - 회원가입 요청을 받아 새로운 사용자를 데이터베이스에 저장
 * - 비밀번호는 반드시 암호화하여 저장 (BCrypt 사용)
 */
@RequiredArgsConstructor // 🔹 Lombok을 사용하여 생성자 주입 자동화
@Service // 🔹 Spring의 Service 컴포넌트로 등록
public class JoinService {

    private final UserRepository userRepository; // 🔹 사용자 정보를 관리하는 JPA Repository
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 🔹 비밀번호 암호화를 위한 BCryptPasswordEncoder

    /**
     * 🔹 회원가입 처리 메서드
     * - 중복된 username이 존재하는지 확인 후 회원가입 진행
     * - 비밀번호는 반드시 BCrypt를 사용하여 암호화 후 저장
     */
    public void join(JoinDto joinDto) {
        // 🔹 중복된 username이 있는지 확인
        Boolean isExist = userRepository.existsByEmail(joinDto.getEmail());

        if (isExist) {
            System.out.println("already exist user"); // 중복 사용자 존재 시 회원가입 진행하지 않음
            return;
        }

        Address address = Address
                .builder()
                .city(joinDto.getCity())
                .street(joinDto.getStreet())
                .zipcode(joinDto.getZipcode())
                .build();

        // 🔹 새로운 사용자 엔터티 생성 및 저장
        UserEntity userEntity = UserEntity
                .builder()
                .email(joinDto.getEmail())
                .username(joinDto.getUsername())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword())) // 비밀번호 암호화 저장
                .role(Role.USER) // 기본적으로 일반 사용자 역할 부여
                .address(address) // 주소 정보 설정
                .build();

        userRepository.save(userEntity); // DB에 사용자 정보 저장
    }

    // ⚠️ 비밀번호는 평문 저장하면 안 되고 반드시 암호화해야 함 (현재 BCrypt 사용)
    // ⚠️ 기본 역할("ROLE_ADMIN") 설정 부분은 필요에 따라 변경 가능
}
