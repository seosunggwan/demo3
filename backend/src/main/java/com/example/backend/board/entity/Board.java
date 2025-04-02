package com.example.backend.board.entity;

import com.example.backend.common.domain.BaseTimeEntity;
import com.example.backend.security.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 엔티티
 * - 게시글 정보를 저장하는 테이블
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;
    
    @Column(nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity author;

    /**
     * 조회수 증가 메서드
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
    
    /**
     * 좋아요 증가 메서드
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }
    
    /**
     * 좋아요 감소 메서드
     */
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 게시글 수정 메서드
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 작성자 이름을 반환하는 메서드
     * 작성자가 없는 경우 "알 수 없음"을 반환
     */
    public String getAuthorName() {
        return author != null ? author.getUsername() : "알 수 없음";
    }
} 