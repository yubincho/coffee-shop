package com.example.coffeeOrderService.domain.payment.repository;

import com.example.coffeeOrderService.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
