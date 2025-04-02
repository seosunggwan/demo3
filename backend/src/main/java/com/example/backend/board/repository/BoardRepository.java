package com.example.backend.board.repository;

import com.example.backend.board.entity.Board;
import com.example.backend.security.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
     * 작성자 이름에 키워드가 포함된 게시글 목록 조회 (페이징)
     */
    Page<Board> findByAuthor_UsernameContaining(String authorName, Pageable pageable);

    /**
     * 인기글 선정을 위한 게시글 리스트 조회 (조회수, 좋아요수 기준)
     * - 점수 계산: (조회수 * 0.3) + (좋아요수 * 0.7)
     * - 최근 하루 동안의 활동 기준
     * 참고: 네이티브 쿼리 사용 시 오류가 발생하여 JPQL로 변경하며, 댓글 수는 서비스에서 처리
     */
    @Query("SELECT b FROM Board b WHERE b.createdTime >= :startDate ORDER BY (b.viewCount * 0.3 + b.likeCount * 0.7) DESC")
    List<Board> findPopularBoardsForDate(@Param("startDate") LocalDateTime startDate, Pageable pageable);
} 