package com.example.coffeeOrderService.dto;


import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDto {

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;
    private Category category;
    private List<ImageDto> images;


    public static ProductDto fromProduct(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice())
                .inventory(product.getInventory())
                .description(product.getDescription())
                .category(product.getCategory()) // Category 변환
                .images(product.getImages().stream()
                        .map(ImageDto::fromImage)
                        .collect(Collectors.toList()))
                .build();
    }
}
