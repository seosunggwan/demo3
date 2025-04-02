package com.example.backend.board.entity;

import com.example.backend.common.domain.BaseTimeEntity;
import com.example.backend.security.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 좋아요 엔티티
 * - 사용자가 어떤 게시글에 좋아요를 눌렀는지 기록하는 테이블
 * - 사용자와 게시글은 다대다 관계가 아닌 일대다 관계로 설계 (한 사용자가 여러 게시글에 좋아요 가능)
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_board_like",
            columnNames = {"board_id", "user_id"}
        )
    }
)
public class BoardLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
} 