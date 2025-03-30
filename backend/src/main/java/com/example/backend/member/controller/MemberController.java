package com.example.backend.member.controller;

import com.example.backend.member.dto.MemberListResDto;
import com.example.backend.member.dto.PagedMembersDto;
import com.example.backend.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member") // 모든 API의 기본 URL이 `/member`로 시작됨
@Slf4j
public class MemberController {
    private final MemberService memberService;

    // 의존성 주입 (MemberService)
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ✅ 회원 목록 조회 API
    @GetMapping("/list")
    public ResponseEntity<?> memberList(){
        List<MemberListResDto> dtos = memberService.findAll(); // 회원 목록 조회
        return new ResponseEntity<>(dtos, HttpStatus.OK); // 조회된 회원 목록 반환
    }
    
    /**
     * 페이지네이션이 적용된 회원 목록 조회 API
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 회원 목록과 페이지 정보
     */
    @GetMapping("/page")
    public ResponseEntity<?> memberListWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("페이지네이션 회원 목록 조회 요청 - 페이지: {}, 사이즈: {}", page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 서비스 호출
        PagedMembersDto result = memberService.findAllWithPaging(page, size);
        
        log.info("페이지 {}의 회원 {}개 조회 완료 (전체 {}개)", page, result.getMembers().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }
    
    /**
     * 이름으로 회원 검색 API (페이지네이션 적용)
     * @param name 검색할 회원 이름 (부분 일치)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 회원 검색 결과와 페이지 정보
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMembersWithPaging(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("페이지네이션 회원 검색 요청 - 이름: {}, 페이지: {}, 사이즈: {}", name, page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 서비스 호출
        PagedMembersDto result = memberService.findByNameWithPaging(name, page, size);
        
        log.info("페이지 {}의 회원 {}개 검색 완료 (전체 {}개)", page, result.getMembers().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }
}