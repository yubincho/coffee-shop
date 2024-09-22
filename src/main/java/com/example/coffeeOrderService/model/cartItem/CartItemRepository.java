package com.example.coffeeOrderService.model.cartItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCartId(Long id);
}
