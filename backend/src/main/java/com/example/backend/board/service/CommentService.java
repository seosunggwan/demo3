package com.example.backend.board.service;

import com.example.backend.board.dto.CommentRequestDto;
import com.example.backend.board.dto.CommentResponseDto;
import com.example.backend.board.entity.Board;
import com.example.backend.board.entity.Comment;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.board.repository.CommentRepository;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 생성
     *
     * @param boardId   게시글 ID
     * @param request   댓글 생성 요청 DTO
     * @param email     사용자 이메일
     * @return          생성된 댓글 DTO
     */
    public CommentResponseDto.CommentDto createComment(Long boardId, CommentRequestDto.CreateRequest request, String email) {
        // 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));
        
        // 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + request.getParentId()));
            
            // 부모 댓글이 삭제되었는지 확인
            if (parent.isDeleted()) {
                throw new IllegalStateException("삭제된 댓글에는 댓글을 달 수 없습니다.");
            }
            
            // 부모 댓글의 게시글 ID와 현재 게시글 ID가 일치하는지 확인
            if (!parent.getBoard().getId().equals(boardId)) {
                throw new IllegalArgumentException("부모 댓글과 게시글이 일치하지 않습니다.");
            }
        }
        
        // 대댓글 제한 (최대 1 depth까지만 허용, 즉 최대 2 depth)
        if (!Comment.canCreateChildComment(parent)) {
            throw new IllegalStateException("대댓글에는 더 이상 댓글을 달 수 없습니다.");
        }
        
        // 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(user)
                .board(board)
                .parent(parent)
                .build();
        
        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);
        log.info("댓글 생성 완료: id={}, 내용={}, 작성자={}", savedComment.getId(), savedComment.getContent(), user.getUsername());
        
        return CommentResponseDto.CommentDto.fromEntity(savedComment);
    }
    
    /**
     * 댓글 수정
     *
     * @param commentId 댓글 ID
     * @param request   댓글 수정 요청 DTO
     * @param email     사용자 이메일
     * @return          수정된 댓글 DTO
     */
    public CommentResponseDto.CommentDto updateComment(Long commentId, CommentRequestDto.UpdateRequest request, String email) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        
        // 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 댓글 작성자 확인
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
        
        // 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }
        
        // 댓글 내용 수정
        comment.updateContent(request.getContent());
        
        // 댓글 저장
        Comment updatedComment = commentRepository.save(comment);
        log.info("댓글 수정 완료: id={}, 내용={}", updatedComment.getId(), updatedComment.getContent());
        
        return CommentResponseDto.CommentDto.fromEntity(updatedComment);
    }
    
    /**
     * 댓글 삭제
     *
     * @param commentId 댓글 ID
     * @param email     사용자 이메일
     */
    public void deleteComment(Long commentId, String email) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        
        // 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // 댓글 작성자 확인
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        
        // 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            throw new IllegalStateException("이미 삭제된 댓글입니다.");
        }
        
        // 소프트 삭제
        comment.delete();
        commentRepository.save(comment);
        log.info("댓글 삭제 완료: id={}", comment.getId());
    }
    
    /**
     * 게시글에 달린 댓글 목록 조회
     *
     * @param boardId 게시글 ID
     * @return        계층 구조의 댓글 목록 DTO
     */
    @Transactional(readOnly = true)
    public CommentResponseDto.CommentListDto getCommentsByBoardId(Long boardId) {
        // 게시글 존재 여부 확인
        if (!boardRepository.existsById(boardId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId);
        }
        
        // 루트 댓글만 조회 (부모 댓글이 없는 댓글)
        List<Comment> rootComments = commentRepository.findRootCommentsByBoardId(boardId);
        
        // 총 댓글 수 조회
        long totalCount = commentRepository.countByBoardId(boardId);
        
        return CommentResponseDto.CommentListDto.fromEntities(rootComments, totalCount);
    }
    
    /**
     * 댓글 조회
     *
     * @param commentId 댓글 ID
     * @return          댓글 DTO
     */
    @Transactional(readOnly = true)
    public CommentResponseDto.CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        
        return CommentResponseDto.CommentDto.fromEntity(comment);
    }
} 