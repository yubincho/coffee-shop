package com.example.coffeeOrderService.domain.order.service;

import com.example.coffeeOrderService.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class OrderLockFacade {

    private final RedissonClient redissonClient;
    private final OrderService orderService;

    public Order placeOrderWithLock(Long userId) {
        // 1) 건드릴 상품 ID (이미 중복 제거 + 오름차순 정렬됨)
        List<Long> sortedIds = orderService.findProductIdsForOrder(userId);

        List<RLock> locks = new ArrayList<>();

        try {
            // 2) 항상 같은 순서로 락 획득 → 데드락 방지
            for (Long productId : sortedIds) {
                RLock lock = redissonClient.getLock("lock:product:" + productId);
                // waitTime: 락 대기 최대 시간, leaseTime: 락 자동 만료 시간
                boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);
                if (!acquired) {
                    throw new IllegalStateException("주문 처리 중 락 획득 실패: product " + productId);
                }
                locks.add(lock);
            }

            // 3) 락을 다 잡은 상태에서 트랜잭션 실행 (재고 차감)
            return orderService.placeOrder(userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트", e);
        } finally {
            // 4) 커밋이 끝난 뒤 락 해제 (역순)
            Collections.reverse(locks);
            for (RLock lock : locks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}

