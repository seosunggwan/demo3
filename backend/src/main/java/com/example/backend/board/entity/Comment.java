package com.example.backend.board.entity;

import com.example.backend.common.domain.BaseTimeEntity;
import com.example.backend.security.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티
 * - 게시글에 대한 댓글 정보를 저장하는 테이블
 * - 계층형 구조를 지원 (최대 2 depth)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public Comment(String content, UserEntity author, Board board, Comment parent) {
        this.content = content;
        this.author = author;
        this.board = board;
        this.deleted = false;
        
        // 부모 댓글이 없으면 depth는 0, 있으면 부모의 depth + 1
        if (parent != null) {
            this.parent = parent;
            this.depth = parent.getDepth() + 1;
            parent.getChildren().add(this);
        } else {
            this.depth = 0;
        }
    }

    /**
     * 댓글 내용 수정
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     * - 실제로 데이터베이스에서 삭제하지 않고, deleted 필드를 true로 설정
     */
    public void delete() {
        this.deleted = true;
    }

    /**
     * 댓글 생성이 가능한지 확인
     * - 최대 depth 체크 (최대 2 depth까지만 허용)
     */
    public static boolean canCreateChildComment(Comment parent) {
        return parent == null || parent.getDepth() < 1;
    }
} 