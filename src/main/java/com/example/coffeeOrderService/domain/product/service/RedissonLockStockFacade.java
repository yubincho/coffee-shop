package com.example.coffeeOrderService.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Service
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;
    private final StockService stockService;

    public void removeStock(Long productId, int quantity) throws InterruptedException {
        RLock lock = redissonClient.getLock("lock:product:" + productId);
        try {
            boolean acquired = lock.tryLock(10, 3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("락 획득 실패: product " + productId);
            }
            stockService.removeStockWithRedisLock(productId, quantity);  // 락 안에서 차감
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
