package com.example.coffeeOrderService.domain.order.service;

import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.domain.order.dto.OrderStatusCache;
import com.example.coffeeOrderService.domain.order.entity.Order;
import com.example.coffeeOrderService.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Qualifier;


import java.time.Duration;


@Slf4j
@RequiredArgsConstructor
@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(10);


    private String key(Long orderId) {
        return "order:status:" + orderId;
    }


    // 캐싱 전 비교용: Redis 건너뛰고 무조건 DB
    public OrderStatusCache getOrderStatusNoCache(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderStatusCache.from(order);
    }


    // 결제 상태 조회: Redis 우선 → miss/장애 시 DB fallback → Redis 재저장
    public OrderStatusCache getOrderStatus(Long orderId) {

        // ① Redis 우선 조회 (장애·타임아웃 나도 죽지 않게 try-catch)
        try {
            Object cached = redisTemplate.opsForValue().get(key(orderId));
            if (cached instanceof OrderStatusCache result) {
                return result;   // HIT → 즉시 응답 (DB 안 감)
            }
        } catch (Exception e) {
            // Redis 장애/타임아웃 → 로그만 남기고 DB로 흘려보냄 (fallback)
            log.warn("Redis 조회 실패, DB fallback 진행. orderId={}, error={}", orderId, e.getMessage());
        }

        // ② MISS 또는 Redis 장애 → MySQL 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatusCache result = OrderStatusCache.from(order);

        // ③ Redis 재저장 (여기서 또 장애나도 응답은 정상으로)
        try {
            redisTemplate.opsForValue().set(key(orderId), result, TTL);
        } catch (Exception e) {
            log.warn("Redis 재저장 실패. orderId={}, error={}", orderId, e.getMessage());
        }

        return result;
    }

    // 상태 변경 시 캐시 무효화
    public void evict(Long orderId) {
        try {
            redisTemplate.delete(key(orderId));
        } catch (Exception e) {
            log.warn("Redis 무효화 실패. orderId={}, error={}", orderId, e.getMessage());
        }
    }
}
