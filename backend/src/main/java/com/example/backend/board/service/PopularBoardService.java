package com.example.backend.board.service;

import com.example.backend.board.dto.PopularBoardDto;
import com.example.backend.board.entity.Board;
import com.example.backend.board.entity.PopularBoard;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.board.repository.PopularBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularBoardService {

    private final BoardRepository boardRepository;
    private final PopularBoardRepository popularBoardRepository;
    
    /**
     * 매일 오전 1시에 인기글을 선정하여 저장합니다.
     * 어제 하루 동안의 활동(조회수, 좋아요수, 댓글수)을 기준으로 점수를 계산합니다.
     */
    @Scheduled(cron = "0 0 1 * * ?") // 매일 오전 1시에 실행
    @Transactional
    public void selectDailyPopularBoards() {
        log.info("인기글 선정 작업 시작: {}", LocalDateTime.now());
        
        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime = today.minusDays(1).atStartOfDay(); // 어제 00:00:00
        
        // 기존 오늘 날짜의 인기글 데이터 삭제 (업데이트 전 초기화)
        popularBoardRepository.deleteBySelectionDate(today);
        
        // 30일 이상 지난 인기글 데이터 삭제 (오래된 데이터 정리)
        popularBoardRepository.deleteBySelectionDateBefore(today.minusDays(30));
        
        // 인기글 계산 및 저장 (상위 10개)
        // PageRequest 객체 생성 (상위 10개만 조회)
        Pageable pageable = PageRequest.of(0, 10);
        List<Board> popularBoards = boardRepository.findPopularBoardsForDate(startDateTime, pageable);
        List<PopularBoard> popularBoardEntities = new ArrayList<>();
        
        int rank = 1;
        for (Board board : popularBoards) {
            // 댓글 수 계산 (댓글 레포지토리에서 조회해야 함, 여기서는 편의상 0으로 설정)
            int commentCount = 0; // 실제로는 commentRepository.countByBoardId(board.getId());
            
            // 점수 계산: (조회수 * 0.3) + (좋아요수 * 0.7) (댓글 수는 제외)
            double score = (board.getViewCount() * 0.3) + (board.getLikeCount() * 0.7);
            
            PopularBoard popularBoard = PopularBoard.builder()
                    .board(board)
                    .selectionDate(today)
                    .rankPosition(rank++)
                    .score(score)
                    .viewCount(board.getViewCount())
                    .likeCount(board.getLikeCount())
                    .commentCount(commentCount)
                    .build();
            
            popularBoardEntities.add(popularBoard);
        }
        
        popularBoardRepository.saveAll(popularBoardEntities);
        log.info("인기글 선정 작업 완료: {}개의 인기글 선정됨", popularBoardEntities.size());
    }
    
    /**
     * 오늘의 인기글 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<PopularBoardDto> getTodayPopularBoards() {
        LocalDate today = LocalDate.now();
        return getPopularBoardsByDate(today);
    }
    
    /**
     * 특정 날짜의 인기글 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<PopularBoardDto> getPopularBoardsByDate(LocalDate date) {
        List<PopularBoard> popularBoards = popularBoardRepository.findBySelectionDateOrderByRankPositionAsc(date);
        
        return popularBoards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 N일간의 인기글 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<PopularBoardDto> getRecentPopularBoards(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<PopularBoard> popularBoards = popularBoardRepository.findRecentPopularBoards(startDate);
        
        return popularBoards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * PopularBoard 엔티티를 DTO로 변환합니다.
     */
    private PopularBoardDto convertToDto(PopularBoard popularBoard) {
        Board board = popularBoard.getBoard();
        return PopularBoardDto.builder()
                .id(popularBoard.getId())
                .boardId(board.getId())
                .title(board.getTitle())
                .authorName(board.getAuthor() != null ? board.getAuthor().getUsername() : "알 수 없음")
                .createdTime(board.getCreatedTime())
                .selectionDate(popularBoard.getSelectionDate())
                .rankPosition(popularBoard.getRankPosition())
                .score(popularBoard.getScore())
                .viewCount(popularBoard.getViewCount())
                .likeCount(popularBoard.getLikeCount())
                .commentCount(popularBoard.getCommentCount())
                .build();
    }
    
    /**
     * 수동으로 인기글 선정 작업을 실행합니다. (테스트 및 긴급 업데이트용)
     */
    @Transactional
    public void runManualPopularBoardSelection() {
        log.info("수동 인기글 선정 작업 시작");
        try {
            selectDailyPopularBoards();
            log.info("수동 인기글 선정 작업 완료");
        } catch (Exception e) {
            log.error("수동 인기글 선정 작업 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("인기글 선정 작업 실패: " + e.getMessage(), e);
        }
    }
} 