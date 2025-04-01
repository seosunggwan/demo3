package com.example.backend.board.dto;

import com.example.backend.board.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentResponseDto {

    /**
     * 단일 댓글 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        private Long id;
        private String content;
        private String authorName;
        private Long authorId;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private int depth;
        private boolean deleted;
        private Long parentId;
        
        @Builder.Default
        private List<CommentDto> children = new ArrayList<>();
        
        /**
         * Comment 엔티티를 CommentDto로 변환
         */
        public static CommentDto fromEntity(Comment comment) {
            if (comment == null) {
                return null;
            }
            
            return CommentDto.builder()
                    .id(comment.getId())
                    .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                    .authorName(comment.getAuthor().getUsername())
                    .authorId(comment.getAuthor().getId())
                    .createdTime(comment.getCreatedTime())
                    .updatedTime(comment.getUpdatedTime())
                    .depth(comment.getDepth())
                    .deleted(comment.isDeleted())
                    .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                    .children(comment.getChildren().stream()
                            .map(CommentDto::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
    
    /**
     * 댓글 목록 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentListDto {
        @Builder.Default
        private List<CommentDto> comments = new ArrayList<>();
        private long totalCount;
        
        /**
         * Comment 엔티티 목록을 CommentListDto로 변환
         */
        public static CommentListDto fromEntities(List<Comment> comments, long totalCount) {
            return CommentListDto.builder()
                    .comments(comments.stream()
                            .map(CommentDto::fromEntity)
                            .collect(Collectors.toList()))
                    .totalCount(totalCount)
                    .build();
        }
    }
} 