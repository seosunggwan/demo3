package com.example.backend.board.controller;

import com.example.backend.board.dto.BoardDto;
import com.example.backend.board.dto.PagedBoardsDto;
import com.example.backend.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    
    private final BoardService boardService;
    
    /**
     * 게시글 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createBoard(
            @Valid @RequestBody BoardDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("게시글 생성 요청: 제목={}, 사용자={}", request.getTitle(), userDetails.getUsername());
        BoardDto.Response response = boardService.create(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * 게시글 수정 API
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("게시글 수정 요청: 게시글 ID={}, 사용자={}", boardId, userDetails.getUsername());
        BoardDto.Response response = boardService.update(boardId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 게시글 삭제 API
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("게시글 삭제 요청: 게시글 ID={}, 사용자={}", boardId, userDetails.getUsername());
        boardService.delete(boardId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 게시글 상세 조회 API
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<?> getBoard(@PathVariable Long boardId) {
        log.info("게시글 상세 조회 요청: 게시글 ID={}", boardId);
        BoardDto.Response response = boardService.getBoard(boardId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 게시글 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<?> getAllBoards() {
        log.info("전체 게시글 목록 조회 요청");
        List<BoardDto.ListResponse> responses = boardService.getAllBoards();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 페이지네이션이 적용된 게시글 목록 조회 API
     */
    @GetMapping("/page")
    public ResponseEntity<?> getBoardsWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("페이지네이션 게시글 목록 조회 요청: 페이지={}, 크기={}", page, size);
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        PagedBoardsDto response = boardService.getBoardsWithPaging(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 키워드로 게시글 검색 API (제목 + 내용)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchBoardsByKeyword(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("게시글 검색 요청: 키워드={}, 페이지={}, 크기={}", keyword, page, size);
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        PagedBoardsDto response = boardService.searchBoardsByKeyword(keyword, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 작성자 이름으로 게시글 검색 API
     */
    @GetMapping("/search/author")
    public ResponseEntity<?> searchBoardsByAuthor(
            @RequestParam(required = false) String authorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("작성자별 게시글 검색 요청: 작성자={}, 페이지={}, 크기={}", authorName, page, size);
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        PagedBoardsDto response = boardService.searchBoardsByAuthor(authorName, page, size);
        return ResponseEntity.ok(response);
    }
} 