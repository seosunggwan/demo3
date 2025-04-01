package com.example.backend.board.controller;

import com.example.backend.board.dto.PopularBoardDto;
import com.example.backend.board.service.PopularBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards/popular")
@RequiredArgsConstructor
@Slf4j
public class PopularBoardController {

    private final PopularBoardService popularBoardService;

    /**
     * 오늘의 인기글 목록 조회
     * - GET /api/boards/popular/today
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayPopularBoards() {
        log.info("오늘의 인기글 목록 조회 요청");
        try {
            List<PopularBoardDto> popularBoards = popularBoardService.getTodayPopularBoards();
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", LocalDate.now());
            response.put("boards", popularBoards);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("인기글 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 특정 날짜의 인기글 목록 조회
     * - GET /api/boards/popular/date/{date}
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getPopularBoardsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("특정 날짜({})의 인기글 목록 조회 요청", date);
        try {
            List<PopularBoardDto> popularBoards = popularBoardService.getPopularBoardsByDate(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("boards", popularBoards);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("인기글 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 최근 N일간의 인기글 목록 조회
     * - GET /api/boards/popular/recent?days=7
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentPopularBoards(
            @RequestParam(defaultValue = "7") int days) {
        log.info("최근 {}일간의 인기글 목록 조회 요청", days);
        try {
            // 일수는 1~30일 범위로 제한
            int daysLimit = Math.min(Math.max(days, 1), 30);
            List<PopularBoardDto> popularBoards = popularBoardService.getRecentPopularBoards(daysLimit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", daysLimit);
            response.put("startDate", LocalDate.now().minusDays(daysLimit - 1));
            response.put("endDate", LocalDate.now());
            response.put("boards", popularBoards);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("인기글 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * 인기글 선정 수동 실행 (관리자 용)
     * - POST /api/boards/popular/refresh
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refreshPopularBoards() {
        log.info("인기글 선정 수동 실행 요청");
        try {
            popularBoardService.runManualPopularBoardSelection();
            return ResponseEntity.ok(Map.of(
                "message", "인기글 선정이 완료되었습니다.",
                "date", LocalDate.now()
            ));
        } catch (Exception e) {
            log.error("인기글 선정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "인기글 선정 중 오류가 발생했습니다: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> calculatePopularBoards() {
        try {
            popularBoardService.selectDailyPopularBoards();
            return ResponseEntity.ok("인기글 결산이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("인기글 결산 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 