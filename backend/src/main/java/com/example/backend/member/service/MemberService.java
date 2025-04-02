package com.example.backend.member.service;

import com.example.backend.security.entity.UserEntity;
import com.example.backend.member.dto.MemberListResDto;
import com.example.backend.member.dto.PagedMembersDto;
import com.example.backend.security.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional // 모든 메서드가 하나의 트랜잭션 단위로 실행됨 (롤백 가능)
public class MemberService {
    private final UserRepository memberRepository;

    // 의존성 주입
    public MemberService(UserRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // ✅ 회원 목록 조회
    public List<MemberListResDto> findAll(){
        List<UserEntity> members = memberRepository.findAll(); // 모든 회원 가져오기
        List<MemberListResDto> memberListResDtos = new ArrayList<>();

        // 회원 객체를 `MemberListResDto`로 변환하여 리스트에 저장
        for (UserEntity m : members){
            MemberListResDto memberListResDto = new MemberListResDto();
            memberListResDto.setId(m.getId());
            memberListResDto.setEmail(m.getEmail());
            memberListResDto.setName(m.getUsername());
            memberListResDtos.add(memberListResDto);
        }

        return memberListResDtos; // 가공된 회원 리스트 반환
    }
    
    /**
     * 페이지네이션이 적용된 회원 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 회원 목록과 페이지 정보
     */
    public PagedMembersDto findAllWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> memberPage = memberRepository.findAll(pageable);
        return new PagedMembersDto(memberPage);
    }
    
    /**
     * 이름으로 회원 검색 (페이지네이션 적용)
     * @param name 검색할 회원 이름 (부분 일치)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 회원 검색 결과와 페이지 정보
     */
    public PagedMembersDto findByNameWithPaging(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> memberPage;
        
        if (name == null || name.trim().isEmpty()) {
            memberPage = memberRepository.findAll(pageable);
        } else {
            memberPage = memberRepository.findByUsernameContaining(name, pageable);
        }
        
        return new PagedMembersDto(memberPage);
    }
}
