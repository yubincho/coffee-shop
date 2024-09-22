package com.example.coffeeOrderService.model.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByProductId(Long productId);
}
