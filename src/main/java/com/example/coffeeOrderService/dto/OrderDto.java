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
    private String merchantUid;   // 주문 번호 (결제 고유 식별자)
    private String itemName;      // 상품명
    private String userEmail;     // 사용자 이메일
    private String username;      // 사용자 이름
    private String address;       // 사용자 주소
    private List<OrderItemDto> items;

    public static OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getOrderId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .status(String.valueOf(order.getOrderStatus()))
                .merchantUid(order.getMerchantUid())     // 주문 번호 추가
                .itemName(order.getOrderItems().stream()
                        .map(item -> item.getProduct().getName()) // 여러 상품일 경우 첫 번째 상품 이름
                        .findFirst().orElse("Unknown Item"))
                .userEmail(order.getUser().getEmail())    // 사용자 이메일 추가
                .username(order.getUser().getNickname())  // 사용자 이름 추가
                .address(order.getUser().getAddress())    // 사용자 주소 추가
                .items(order.getOrderItems().stream()
                        .map(OrderItemDto::toDto)
                        .toList())
                .build();
    }
}
