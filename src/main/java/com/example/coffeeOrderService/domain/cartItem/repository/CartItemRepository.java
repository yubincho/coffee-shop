package com.example.coffeeOrderService.domain.cartItem.repository;

import com.example.coffeeOrderService.domain.cartItem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCartId(Long id);

    @Query("select ci.product.id from CartItem ci where ci.cart.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);
}
