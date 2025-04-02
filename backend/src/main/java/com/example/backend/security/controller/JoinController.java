package com.example.backend.security.controller;

import com.example.backend.security.dto.form.JoinDto;
import com.example.backend.security.service.form.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController // 이 클래스가 REST 컨트롤러임을 나타냄 (JSON 응답을 반환)
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입 자동화
public class JoinController {

    private final JoinService joinService; // 회원가입 관련 서비스

    @PostMapping("/join") // HTTP POST 요청을 "/join" 경로에서 처리
    public ResponseEntity<?> joinProc(@RequestBody JoinDto joinDto) {
        try {
            joinService.join(joinDto); // JoinService를 통해 회원가입 로직 실행
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
