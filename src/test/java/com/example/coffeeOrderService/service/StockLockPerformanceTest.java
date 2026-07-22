package com.example.coffeeOrderService.service;

import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import com.example.coffeeOrderService.domain.product.service.OptimisticLockStockFacade;
import com.example.coffeeOrderService.domain.product.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class StockLockPerformanceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    private Long productId;

    private static final int THREAD_COUNT = 100;
    private static final int POOL_SIZE = 32;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("아메리카노")
                .brand("커피")
                .price(new BigDecimal("4000"))
                .inventory(THREAD_COUNT)   // 재고 = 요청 수
                .description("성능 테스트 상품")
                .build();
        productId = productRepository.save(product).getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(productId);
    }

    @Test
    void 비관적_락_성능_측정() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();   // 측정 시작

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    stockService.removeStockWithPessimisticLock(productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();     // 측정 끝

        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);   // 정합성도 함께 확인

        System.out.println("=== 비관적 락 소요 시간: " + (end - start) + "ms ===");
    }

    @Test
    void 낙관적_락_성능_측정() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.removeStock(productId, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();

        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);

        System.out.println("=== 낙관적 락 소요 시간: " + (end - start) + "ms ===");
    }
}
