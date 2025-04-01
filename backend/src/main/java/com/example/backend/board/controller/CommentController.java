package com.example.backend.board.controller;

import com.example.backend.board.dto.CommentRequestDto;
import com.example.backend.board.dto.CommentResponseDto;
import com.example.backend.board.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 댓글 관련 API 컨트롤러
 * - 게시글에 대한 댓글 CRUD 기능 제공
 * - URI: /api/boards/{boardId}/comments
 */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CommentRequestDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("댓글 생성 요청: 게시글 ID={}, 내용={}, 사용자={}", 
                boardId, request.getContent(), userDetails.getUsername());
        
        try {
            CommentResponseDto.CommentDto response = 
                    commentService.createComment(boardId, request, userDetails.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("댓글 생성 실패: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 수정 API
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("댓글 수정 요청: 게시글 ID={}, 댓글 ID={}, 사용자={}", 
                boardId, commentId, userDetails.getUsername());
        
        try {
            CommentResponseDto.CommentDto response = 
                    commentService.updateComment(commentId, request, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 삭제 API
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("댓글 삭제 요청: 게시글 ID={}, 댓글 ID={}, 사용자={}", 
                boardId, commentId, userDetails.getUsername());
        
        try {
            commentService.deleteComment(commentId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 게시글에 달린 댓글 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<?> getCommentsByBoardId(@PathVariable Long boardId) {
        log.info("댓글 목록 조회 요청: 게시글 ID={}", boardId);
        
        try {
            CommentResponseDto.CommentListDto response = commentService.getCommentsByBoardId(boardId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 상세 조회 API
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(
            @PathVariable Long boardId,
            @PathVariable Long commentId) {
        
        log.info("댓글 상세 조회 요청: 게시글 ID={}, 댓글 ID={}", boardId, commentId);
        
        try {
            CommentResponseDto.CommentDto response = commentService.getCommentById(commentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("댓글 상세 조회 실패: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 