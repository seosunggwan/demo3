package com.example.backend.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "popular_boards", indexes = {
    @Index(name = "idx_popular_board_date", columnList = "selection_date"),
    @Index(name = "idx_popular_board_rank_position", columnList = "rank_position")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "selection_date", nullable = false)
    private LocalDate selectionDate;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;
} 