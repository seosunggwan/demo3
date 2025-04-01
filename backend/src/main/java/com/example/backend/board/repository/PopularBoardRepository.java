package com.example.backend.board.repository;

import com.example.backend.board.entity.PopularBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PopularBoardRepository extends JpaRepository<PopularBoard, Long> {

    /**
     * 특정 날짜의 인기글 목록을 조회합니다 (순위 오름차순)
     */
    List<PopularBoard> findBySelectionDateOrderByRankPositionAsc(LocalDate date);
    
    /**
     * 최근 N일간의 인기글 목록을 조회합니다 (날짜 내림차순, 순위 오름차순)
     */
    @Query("SELECT p FROM PopularBoard p WHERE p.selectionDate >= :startDate ORDER BY p.selectionDate DESC, p.rankPosition ASC")
    List<PopularBoard> findRecentPopularBoards(@Param("startDate") LocalDate startDate);
    
    /**
     * 특정 날짜의 인기글 데이터를 모두 삭제합니다 (업데이트 전 초기화용)
     */
    void deleteBySelectionDate(LocalDate date);
    
    /**
     * 특정 기간 이전의 데이터를 모두 삭제합니다 (오래된 데이터 정리용)
     */
    void deleteBySelectionDateBefore(LocalDate date);
} 