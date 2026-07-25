package com.example.coffeeOrderService.domain.cart.repository;

import com.example.coffeeOrderService.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c " +
            "JOIN FETCH c.cartItems ci " +
            "JOIN FETCH ci.product p " +
            "LEFT JOIN FETCH p.images " +        // ← 이 줄 추가!
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItemsAndProducts(@Param("userId") Long userId);


    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems WHERE c.user.id = :userId")
    Optional<Cart> findByIdWithItems(@Param("userId") Long userId);

}
