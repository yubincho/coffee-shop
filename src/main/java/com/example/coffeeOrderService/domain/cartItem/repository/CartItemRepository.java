package com.example.coffeeOrderService.domain.cartItem.repository;

import com.example.coffeeOrderService.domain.cartItem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCartId(Long id);
}
