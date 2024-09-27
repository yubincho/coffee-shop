package com.example.coffeeOrderService.dto;

import com.example.coffeeOrderService.model.orderItem.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemDto {
    private Long productId;
    private String productName;
    private String productBrand;
    private int quantity;
    private BigDecimal price;


    public static OrderItemDto toDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productBrand(orderItem.getProduct().getBrand())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}
