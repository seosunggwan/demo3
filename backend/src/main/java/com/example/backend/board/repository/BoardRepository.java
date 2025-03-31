package com.example.backend.board.repository;

import com.example.backend.board.entity.Board;
import com.example.backend.securitylogin.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    
    /**
     * 작성자로 게시글 목록 조회
     */
    List<Board> findByAuthor(UserEntity author);
    
    /**
     * 제목에 키워드가 포함된 게시글 목록 조회 (페이징)
     */
    Page<Board> findByTitleContaining(String keyword, Pageable pageable);
    
    /**
     * 내용에 키워드가 포함된 게시글 목록 조회 (페이징)
     */
    Page<Board> findByContentContaining(String keyword, Pageable pageable);
    
    /**
     * 제목 또는 내용에 키워드가 포함된 게시글 목록 조회 (페이징)
     */
    @Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword%")
    Page<Board> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 작성자 이름으로 게시글 목록 조회 (페이징)
     */
    Page<Board> findByAuthor_UsernameContaining(String username, Pageable pageable);
} 