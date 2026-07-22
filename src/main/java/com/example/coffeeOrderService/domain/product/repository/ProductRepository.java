package com.example.coffeeOrderService.domain.product.repository;

import com.example.coffeeOrderService.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, QuerydslPredicateExecutor<Product>, ProductRepositoryCustom {

    Page<Product> findAll(Pageable pageable);

    boolean existsByNameAndBrand(String name, String brand);

    // 비관적 락: 조회하는 순간 해당 행에 쓰기 락을 건다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

    // 낙관적 락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithOptimisticLock(@Param("id") Long id);

}
