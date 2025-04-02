package com.example.backend.board.dto;

import java.time.LocalDateTime;

import com.example.backend.board.entity.Board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BoardDto {
    
    /**
     * 게시글 생성 요청 DTO
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수 항목입니다.")
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해주세요.")
        private String title;
        
        @NotBlank(message = "내용은 필수 항목입니다.")
        @Size(min = 10, message = "내용은 10자 이상 입력해주세요.")
        private String content;
    }
    
    /**
     * 게시글 수정 요청 DTO
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "제목은 필수 항목입니다.")
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해주세요.")
        private String title;
        
        @NotBlank(message = "내용은 필수 항목입니다.")
        @Size(min = 10, message = "내용은 10자 이상 입력해주세요.")
        private String content;
    }
    
    /**
     * 게시글 상세 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String authorName;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private int viewCount;
        private int likeCount;
        private boolean isAuthor;
        private boolean isLiked;
        
        /**
         * Board 엔티티와 추가 정보로 Response 객체 생성
         */
        public static Response fromEntity(Board board, boolean isAuthor, boolean isLiked) {
            String authorName = board.getAuthor() != null ? board.getAuthor().getUsername() : "알 수 없음";
            
            return Response.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .authorName(authorName)
                    .createdTime(board.getCreatedTime())
                    .updatedTime(board.getUpdatedTime())
                    .viewCount(board.getViewCount())
                    .likeCount(board.getLikeCount())
                    .isAuthor(isAuthor)
                    .isLiked(isLiked)
                    .build();
        }
    }
    
    /**
     * 게시글 목록 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String title;
        private String authorName;
        private LocalDateTime createdTime;
        private int viewCount;
        private int likeCount;
        private int commentCount;
        
        /**
         * Board 엔티티와 댓글 수로 ListResponse 객체 생성
         */
        public static ListResponse fromEntity(Board board, int commentCount) {
            String authorName = board.getAuthor() != null ? board.getAuthor().getUsername() : "알 수 없음";
            
            return ListResponse.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .authorName(authorName)
                    .createdTime(board.getCreatedTime())
                    .viewCount(board.getViewCount())
                    .likeCount(board.getLikeCount())
                    .commentCount(commentCount)
                    .build();
        }
    }
} 