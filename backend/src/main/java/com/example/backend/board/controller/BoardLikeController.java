package com.example.backend.board.controller;

import com.example.backend.board.service.BoardLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardLikeController {
    
    private final BoardLikeService boardLikeService;
    
    /**
     * 좋아요 토글 API
     * - POST /api/boards/{boardId}/likes
     */
    @PostMapping("/{boardId}/likes")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("좋아요 토글 요청: 게시글 ID={}, 사용자={}", boardId, userDetails.getUsername());
        
        try {
            boolean isLiked = boardLikeService.toggleLike(boardId, userDetails.getUsername());
            long likeCount = boardLikeService.getLikeCount(boardId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("좋아요 처리 중 오류 발생: {}", e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 좋아요 상태 확인 API
     * - GET /api/boards/{boardId}/likes/status
     */
    @GetMapping("/{boardId}/likes/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("좋아요 상태 확인 요청: 게시글 ID={}, 사용자={}", boardId, userDetails.getUsername());
        
        try {
            boolean isLiked = boardLikeService.isLiked(boardId, userDetails.getUsername());
            long likeCount = boardLikeService.getLikeCount(boardId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("좋아요 상태 확인 중 오류 발생: {}", e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 좋아요 개수 조회 API
     * - GET /api/boards/{boardId}/likes/count
     */
    @GetMapping("/{boardId}/likes/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long boardId) {
        log.info("좋아요 개수 조회 요청: 게시글 ID={}", boardId);
        
        try {
            long likeCount = boardLikeService.getLikeCount(boardId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("좋아요 개수 조회 중 오류 발생: {}", e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 