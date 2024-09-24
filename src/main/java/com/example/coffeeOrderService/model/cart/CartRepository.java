package com.example.coffeeOrderService.model.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByUserId(Long userId);

    @Query("SELECT c FROM Cart c " +
            "JOIN FETCH c.cartItems ci " +
            "JOIN FETCH ci.product p " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItemsAndProducts(@Param("userId") Long userId);

}
