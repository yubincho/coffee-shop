package com.example.coffeeOrderService.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class OptimisticLockStockFacade {

    private final StockService stockService;

    public void removeStock(Long productId, int quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.removeStockWithOptimisticLock(productId, quantity);
                break;  // 성공하면 탈출
            } catch (ObjectOptimisticLockingFailureException e) {
                // 충돌 발생 → 잠깐 쉬고 재시도
                Thread.sleep(50);
            }
        }
    }

}
