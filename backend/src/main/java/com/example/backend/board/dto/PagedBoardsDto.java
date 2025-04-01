package com.example.backend.board.dto;

import com.example.backend.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedBoardsDto {
    
    private List<BoardDto.ListResponse> boards;
    private PageInfo pageInfo;
    
    public PagedBoardsDto(Page<Board> boardPage) {
        this.boards = boardPage.getContent().stream()
                .map(board -> BoardDto.ListResponse.fromEntity(board, 0))
                .collect(Collectors.toList());
        
        this.pageInfo = new PageInfo(
                boardPage.getNumber(),
                boardPage.getSize(),
                boardPage.getTotalElements(),
                boardPage.getTotalPages(),
                boardPage.isFirst(),
                boardPage.isLast(),
                boardPage.hasNext(),
                boardPage.hasPrevious()
        );
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
} 