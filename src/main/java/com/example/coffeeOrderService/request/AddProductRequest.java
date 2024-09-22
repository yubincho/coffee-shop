package com.example.coffeeOrderService.request;

import com.example.coffeeOrderService.model.category.Category;
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
