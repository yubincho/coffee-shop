package com.example.coffeeOrderService.request;

import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.product.ProductStatus;
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
