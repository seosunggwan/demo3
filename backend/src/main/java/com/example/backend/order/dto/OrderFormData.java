package com.example.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderFormData {
    private List<MemberDto> members;
    private List<ItemDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDto {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private Long id;
        private String name;
        private int price;
        private int stockQuantity;
    }
} 