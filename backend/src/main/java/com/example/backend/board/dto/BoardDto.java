package com.example.backend.board.dto;

import com.example.backend.board.entity.Board;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BoardDto {
    
    /**
     * 게시글 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수 입력 항목입니다")
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해주세요")
        private String title;
        
        @NotBlank(message = "내용은 필수 입력 항목입니다")
        private String content;
    }
    
    /**
     * 게시글 수정 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "제목은 필수 입력 항목입니다")
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해주세요")
        private String title;
        
        @NotBlank(message = "내용은 필수 입력 항목입니다")
        private String content;
    }
    
    /**
     * 게시글 상세 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String authorName;
        private Long authorId;
        private int viewCount;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        
        /**
         * 엔티티를 DTO로 변환
         */
        public static Response fromEntity(Board board) {
            return Response.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .authorName(board.getAuthor().getUsername())
                    .authorId(board.getAuthor().getId())
                    .viewCount(board.getViewCount())
                    .createdTime(board.getCreatedTime())
                    .updatedTime(board.getUpdatedTime())
                    .build();
        }
    }
    
    /**
     * 게시글 목록 조회 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String title;
        private String authorName;
        private int viewCount;
        private LocalDateTime createdTime;
        
        /**
         * 엔티티를 DTO로 변환
         */
        public static ListResponse fromEntity(Board board) {
            return ListResponse.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .authorName(board.getAuthor().getUsername())
                    .viewCount(board.getViewCount())
                    .createdTime(board.getCreatedTime())
                    .build();
        }
    }
} 