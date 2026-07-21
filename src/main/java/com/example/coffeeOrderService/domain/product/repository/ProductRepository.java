package com.example.coffeeOrderService.domain.product.repository;

import com.example.coffeeOrderService.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, QuerydslPredicateExecutor<Product>, ProductRepositoryCustom {

    Page<Product> findAll(Pageable pageable);

    boolean existsByNameAndBrand(String name, String brand);
}
