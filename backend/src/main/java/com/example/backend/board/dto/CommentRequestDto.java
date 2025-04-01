package com.example.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentRequestDto {

    /**
     * 댓글 생성 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(min = 1, max = 1000, message = "댓글 내용은 1자 이상 1000자 이하여야 합니다.")
        private String content;
        
        private Long parentId; // 부모 댓글 ID (대댓글인 경우). null이면 최상위 댓글
    }
    
    /**
     * 댓글 수정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(min = 1, max = 1000, message = "댓글 내용은 1자 이상 1000자 이하여야 합니다.")
        private String content;
    }
} 