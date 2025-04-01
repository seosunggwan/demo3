package com.example.backend.board.repository;

import com.example.backend.board.entity.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 게시글에 달린 최상위 댓글 목록 조회 (부모 댓글이 없는 댓글)
     */
    @Query("SELECT c FROM Comment c WHERE c.board.id = :boardId AND c.parent IS NULL ORDER BY c.createdTime ASC")
    List<Comment> findRootCommentsByBoardId(@Param("boardId") Long boardId);
    
    /**
     * 게시글에 달린 모든 댓글 목록 조회
     */
    List<Comment> findByBoardId(Long boardId, Sort sort);
    
    /**
     * 부모 댓글로 자식 댓글 목록 조회
     */
    List<Comment> findByParentId(Long parentId, Sort sort);
    
    /**
     * 특정 사용자가 작성한 댓글 목록 조회
     */
    List<Comment> findByAuthorId(Long authorId, Sort sort);
    
    /**
     * 게시글 ID와 댓글 작성자 ID로 댓글 목록 조회
     */
    List<Comment> findByBoardIdAndAuthorId(Long boardId, Long authorId, Sort sort);
    
    /**
     * 게시글 ID로 댓글 수 조회
     */
    long countByBoardId(Long boardId);
} 