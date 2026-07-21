package com.example.coffeeOrderService.domain.orderItem.repository;

import com.example.coffeeOrderService.domain.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
