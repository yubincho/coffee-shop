package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.dto.CartDto;
import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.response.ApiResponse;
import com.example.coffeeOrderService.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {

    private final CartService cartService;


    @GetMapping("/user/{userId}/my-cart")
    public ResponseEntity<ApiResponse> getCart(@PathVariable("userId") Long userId) {
        try {
            Cart cart = cartService.getCartByUserId(userId);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok().body(new ApiResponse("Success", cartDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @GetMapping("/{cartId}/cart/total-price")
    public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable("cartId") Long cartId) {
        try {
            BigDecimal totalPrice = cartService.getTotalPrice(cartId);
            return ResponseEntity.ok().body(new ApiResponse("Success", totalPrice));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable("cartId") Long cartId) {
        try {
            cartService.clearCart(cartId);
            return ResponseEntity.ok().body(new ApiResponse("Cart removed successfully!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }


}
