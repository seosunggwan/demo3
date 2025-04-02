package com.example.backend.board.service;

import com.example.backend.board.entity.Board;
import com.example.backend.board.entity.BoardLike;
import com.example.backend.board.repository.BoardLikeRepository;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardLikeService {
    
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final UserRepository userRepository;
    
    /**
     * 좋아요 토글 (추가 또는 삭제)
     * 
     * @param boardId 게시글 ID
     * @param email 사용자 이메일
     * @return 현재 좋아요 상태 (true: 좋아요 상태, false: 좋아요 취소 상태)
     */
    public boolean toggleLike(Long boardId, String email) {
        // 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));
        
        // 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 이미 좋아요를 눌렀는지 확인
        Optional<BoardLike> existingLike = boardLikeRepository.findByBoardAndUser(board, user);
        
        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀다면 좋아요 취소
            boardLikeRepository.delete(existingLike.get());
            board.decreaseLikeCount();
            boardRepository.save(board);
            log.info("좋아요 취소: 게시글 ID={}, 사용자={}", boardId, email);
            return false;
        } else {
            // 좋아요를 누르지 않았다면 좋아요 추가
            BoardLike boardLike = BoardLike.builder()
                    .board(board)
                    .user(user)
                    .build();
            boardLikeRepository.save(boardLike);
            board.increaseLikeCount();
            boardRepository.save(board);
            log.info("좋아요 추가: 게시글 ID={}, 사용자={}", boardId, email);
            return true;
        }
    }
    
    /**
     * 좋아요 상태 확인
     * 
     * @param boardId 게시글 ID
     * @param email 사용자 이메일
     * @return 좋아요 상태 (true: 좋아요 눌렀음, false: 좋아요 누르지 않음)
     */
    @Transactional(readOnly = true)
    public boolean isLiked(Long boardId, String email) {
        // 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 좋아요 여부 확인
        return boardLikeRepository.existsByBoardIdAndUserId(boardId, user.getId());
    }
    
    /**
     * 게시글의 좋아요 개수 조회
     * 
     * @param boardId 게시글 ID
     * @return 좋아요 개수
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long boardId) {
        // 게시글 존재 여부 확인
        if (!boardRepository.existsById(boardId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId);
        }
        
        // 좋아요 개수 조회
        return boardLikeRepository.countByBoardId(boardId);
    }
} 