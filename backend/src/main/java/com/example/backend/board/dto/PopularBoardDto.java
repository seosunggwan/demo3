package com.example.backend.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBoardDto {

    private Long id;            // 인기글 ID
    private Long boardId;       // 원본 게시글 ID
    private String title;       // 게시글 제목
    private String authorName;  // 작성자
    private LocalDateTime createdTime; // 게시글 작성일시
    private LocalDate selectionDate;   // 인기글 선정일
    private int rankPosition;   // 순위
    private double score;       // 점수
    private int viewCount;      // 조회수
    private int likeCount;      // 좋아요수
    private int commentCount;   // 댓글수
} 