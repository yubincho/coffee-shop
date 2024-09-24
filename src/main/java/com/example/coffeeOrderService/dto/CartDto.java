package com.example.coffeeOrderService.dto;

import com.example.coffeeOrderService.model.cart.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartDto {
    private Long cartId;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;


    public static CartDto fromCart(Cart cart) {
        return CartDto.builder()
                .cartId(cart.getId())
                .totalAmount(cart.getTotalAmount())
                // CartItemDto 리스트 변환
                .items(cart.getCartItems().stream()
                        .map(CartItemDto::fromCartItem)  // CartItem을 CartItemDto로 변환
                        .collect(Collectors.toList()))
                .build();
    }
}
