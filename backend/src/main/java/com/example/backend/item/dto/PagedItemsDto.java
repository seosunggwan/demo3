package com.example.backend.item.dto;

import com.example.backend.item.domain.Item;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 페이지네이션이 적용된 상품 목록 응답 DTO
 */
@Getter
public class PagedItemsDto {
    private List<ItemDto> items;
    private PageInfo pageInfo;

    public PagedItemsDto(List<Item> items, int page, int size, long total) {
        this.items = items.stream()
                .map(item -> ItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .stockQuantity(item.getStockQuantity())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
        
        this.pageInfo = new PageInfo(page, size, total);
    }

    /**
     * 페이지 정보를 담는 내부 클래스
     */
    @Getter
    public static class PageInfo {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageInfo(int page, int size, long total) {
            this.page = page;
            this.size = size;
            this.total = total;
            this.totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
            this.hasNext = page < totalPages - 1;
            this.hasPrevious = page > 0;
        }
    }
} 