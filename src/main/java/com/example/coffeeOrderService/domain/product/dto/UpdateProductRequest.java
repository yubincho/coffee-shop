package com.example.coffeeOrderService.domain.product.dto;

import com.example.coffeeOrderService.domain.category.entity.Category;
import com.example.coffeeOrderService.domain.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class UpdateProductRequest {

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;

    private ProductStatus status;

    private Category category;

}
