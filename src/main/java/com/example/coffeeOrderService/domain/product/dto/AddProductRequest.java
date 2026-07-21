package com.example.coffeeOrderService.domain.product.dto;

import com.example.coffeeOrderService.domain.category.entity.Category;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Setter
@Getter
public class AddProductRequest {

//    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;

    private Category category;

}
