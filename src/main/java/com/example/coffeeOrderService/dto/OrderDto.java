package com.example.coffeeOrderService.dto;

import com.example.coffeeOrderService.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Builder
@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;

    public static OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getOrderId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .status(String.valueOf(order.getOrderStatus()))
                .items(order.getOrderItems().stream()
                        .map(OrderItemDto::toDto)
                        .toList())
                .build();
    }
}
