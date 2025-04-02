package com.example.backend.board.repository;

import com.example.backend.board.entity.Board;
import com.example.backend.board.entity.BoardLike;
import com.example.backend.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    
    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     */
    Optional<BoardLike> findByBoardAndUser(Board board, UserEntity user);
    
    /**
     * 특정 게시글의 좋아요 개수 조회
     */
    long countByBoardId(Long boardId);
    
    /**
     * 사용자 ID와 게시글 ID로 좋아요 존재 여부 확인
     */
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    
    /**
     * 사용자 ID와 게시글 ID로 좋아요 삭제
     */
    void deleteByBoardIdAndUserId(Long boardId, Long userId);
    
    /**
     * 특정 사용자가 누른 좋아요 개수 조회
     */
    long countByUserId(Long userId);
} 