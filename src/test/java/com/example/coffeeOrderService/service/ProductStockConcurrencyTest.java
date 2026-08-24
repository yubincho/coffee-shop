package com.example.coffeeOrderService.service;

import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import com.example.coffeeOrderService.domain.product.service.OptimisticLockStockFacade;
import com.example.coffeeOrderService.domain.product.service.RedissonLockStockFacade;
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
public class ProductStockConcurrencyTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;   // 얇은 서비스

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockService;

    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

    private Long productId;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("아메리카노")
                .brand("커피")
                .price(new BigDecimal("4000"))
                .inventory(100)          // 재고 100개로 시작
                .description("테스트 상품")
                .build();
        productId = productRepository.save(product).getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(productId);
    }


    @Test
    void 동시에_100개_주문하면_재고가_0이_되어야_한다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.removeStock(productId, 1);  // 각 스레드가 재고 1개씩 차감
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 100개 스레드가 다 끝날 때까지 대기

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);  // 기대: 0
    }


    @Test
    void 비관적_락_동시에_100개_주문하면_재고가_0이_된다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.removeStockWithPessimisticLock(productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);
    }


    @Test
    void 낙관적_락_동시에_100개_주문하면_재고가_0이_된다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockService.removeStock(productId, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);
    }

    @Test
    void Redisson_락_동시에_100개_주문하면_재고가_0이_된다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockStockFacade.removeStock(productId, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getInventory()).isEqualTo(0);
    }



}
