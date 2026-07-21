package com.example.coffeeOrderService.domain.cartItem.dto;

import com.example.coffeeOrderService.domain.cartItem.entity.CartItem;
import com.example.coffeeOrderService.domain.product.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItemDto {
    private Long itemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private ProductDto product;


    public static CartItemDto fromCartItem(CartItem cartItem) {
        return CartItemDto.builder()
                .itemId(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .product(ProductDto.fromProduct(cartItem.getProduct()))  // Product 변환
                .build();
    }
}
