package com.example.coffeeOrderService.model.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndBrand(String name, String brand);
}
