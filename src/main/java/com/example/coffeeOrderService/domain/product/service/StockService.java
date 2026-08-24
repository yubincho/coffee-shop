package com.example.coffeeOrderService.domain.product.service;

import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class StockService {

    private final ProductRepository productRepository;

    @Transactional
    public void removeStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.removeStock(quantity);   // 앞서 Product에 만든 메서드
        productRepository.save(product);
    }


    @Transactional
    public void removeStockWithPessimisticLock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.removeStock(quantity);
        // 더티 체킹으로 자동 반영
    }


    @Transactional
    public void removeStockWithOptimisticLock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithOptimisticLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.removeStock(quantity);
    }


    // Redis 락 버전에서 쓸 순수 재고 차감 (락은 Facade가 트랜잭션 밖에서 담당)
    @Transactional
    public void removeStockWithRedisLock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)  // 락 없는 일반 조회
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.removeStock(quantity);
        // 더티 체킹으로 자동 반영
    }
}
