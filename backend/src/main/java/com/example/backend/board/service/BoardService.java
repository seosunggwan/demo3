package com.example.backend.board.service;

import com.example.backend.board.dto.BoardDto;
import com.example.backend.board.dto.PagedBoardsDto;
import com.example.backend.board.entity.Board;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardService {
    
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    
    /**
     * 게시글 생성
     */
    public BoardDto.Response create(BoardDto.CreateRequest request, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)
                .viewCount(0)
                .build();
        
        Board savedBoard = boardRepository.save(board);
        log.info("게시글 생성 완료: id={}, 제목={}, 작성자={}", savedBoard.getId(), savedBoard.getTitle(), user.getUsername());
        
        return BoardDto.Response.fromEntity(savedBoard, true, false);
    }
    
    /**
     * 게시글 수정
     */
    public BoardDto.Response update(Long boardId, BoardDto.UpdateRequest request, String email) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));
        
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 작성자 확인
        if (!board.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("게시글 수정 권한이 없습니다.");
        }
        
        // 게시글 수정
        board.update(request.getTitle(), request.getContent());
        Board updatedBoard = boardRepository.save(board);
        log.info("게시글 수정 완료: id={}, 제목={}", updatedBoard.getId(), updatedBoard.getTitle());
        
        return BoardDto.Response.fromEntity(updatedBoard, true, false);
    }
    
    /**
     * 게시글 삭제
     */
    public void delete(Long boardId, String email) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));
        
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 작성자 확인
        if (!board.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("게시글 삭제 권한이 없습니다.");
        }
        
        boardRepository.delete(board);
        log.info("게시글 삭제 완료: id={}, 제목={}", board.getId(), board.getTitle());
    }
    
    /**
     * 게시글 상세 조회
     */
    @Transactional
    public BoardDto.Response getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));
        
        // 조회수 증가
        board.increaseViewCount();
        Board updatedBoard = boardRepository.save(board);
        
        return BoardDto.Response.fromEntity(updatedBoard, false, false);
    }
    
    /**
     * 전체 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardDto.ListResponse> getAllBoards() {
        List<Board> boards = boardRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        
        return boards.stream()
                .map(board -> BoardDto.ListResponse.fromEntity(board, 0))
                .collect(Collectors.toList());
    }
    
    /**
     * 페이지네이션이 적용된 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public PagedBoardsDto getBoardsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boardPage = boardRepository.findAll(pageable);
        
        return new PagedBoardsDto(boardPage);
    }
    
    /**
     * 키워드로 게시글 검색 (제목 + 내용)
     */
    @Transactional(readOnly = true)
    public PagedBoardsDto searchBoardsByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boardPage;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            boardPage = boardRepository.findAll(pageable);
        } else {
            boardPage = boardRepository.findByTitleOrContentContaining(keyword, pageable);
        }
        
        return new PagedBoardsDto(boardPage);
    }
    
    /**
     * 작성자 이름으로 게시글 검색
     */
    @Transactional(readOnly = true)
    public PagedBoardsDto searchBoardsByAuthor(String authorName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boardPage;
        
        if (authorName == null || authorName.trim().isEmpty()) {
            boardPage = boardRepository.findAll(pageable);
        } else {
            boardPage = boardRepository.findByAuthor_UsernameContaining(authorName, pageable);
        }
        
        return new PagedBoardsDto(boardPage);
    }
    
    /**
     * 게시글의 작성자 이름을 가져옵니다.
     * 작성자가 없는 경우 "알 수 없음"을 반환합니다.
     */
    @SuppressWarnings("unused")
    private String getAuthorName(Board board) {
        return board.getAuthor() != null ? board.getAuthor().getUsername() : "알 수 없음";
    }
} 