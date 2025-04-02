package com.example.backend.member.dto;

import com.example.backend.security.entity.UserEntity;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 페이지네이션이 적용된 회원 목록 응답 DTO
 */
@Getter
public class PagedMembersDto {
    private List<MemberListResDto> members;
    private PageInfo pageInfo;

    public PagedMembersDto(Page<UserEntity> memberPage) {
        this.members = memberPage.getContent().stream()
                .map(member -> {
                    MemberListResDto dto = new MemberListResDto();
                    dto.setId(member.getId());
                    dto.setEmail(member.getEmail());
                    dto.setName(member.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
        
        this.pageInfo = new PageInfo(
                memberPage.getNumber(),
                memberPage.getSize(),
                memberPage.getTotalElements(),
                memberPage.getTotalPages(),
                memberPage.hasNext(),
                memberPage.hasPrevious()
        );
    }

    /**
     * 페이지 정보를 담는 내부 클래스
     */
    @Getter
    public static class PageInfo {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageInfo(int page, int size, long total, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.page = page;
            this.size = size;
            this.total = total;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }
    }
} 