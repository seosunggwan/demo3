package com.example.backend.order.dto;

import com.example.backend.delivery.DeliveryStatus;
import com.example.backend.order.Order;
import com.example.backend.order.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String memberName;
    private String memberEmail;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private List<OrderItemDto> orderItems;
    private DeliveryStatus deliveryStatus;

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();
        this.memberName = order.getMember().getUsername();
        this.memberEmail = order.getMember().getEmail();
        this.orderStatus = order.getStatus();
        this.orderDate = order.getOrderDate();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
        this.deliveryStatus = order.getDelivery().getStatus();
    }

    @Getter
    @NoArgsConstructor
    public static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(com.example.backend.order.OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
} 