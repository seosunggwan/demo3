package com.example.backend.order.dto;

import com.example.backend.order.Order;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 페이지네이션이 적용된 주문 목록 응답 DTO
 */
@Getter
public class PagedOrdersDto {
    private List<OrderResponseDto> orders;
    private PageInfo pageInfo;

    public PagedOrdersDto(List<Order> orders, int page, int size, long total) {
        this.orders = orders.stream()
                .map(OrderResponseDto::new)
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