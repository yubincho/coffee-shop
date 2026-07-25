package com.example.coffeeOrderService.domain.product.dto;


import com.example.coffeeOrderService.domain.category.entity.Category;
import com.example.coffeeOrderService.domain.image.dto.ImageDto;
import com.example.coffeeOrderService.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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
    private String productStatus;
    private String description;

    // 변경: Category 엔티티 대신 필요한 값만 담음
    private Long categoryId;
    private String categoryName;

    private List<ImageDto> images;


    public static ProductDto fromProduct(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice())
                .inventory(product.getInventory())
                .productStatus(String.valueOf(product.getStatus()))
                .description(product.getDescription())
                // 변경: null 방어 + 값을 즉시 꺼내서 지연 로딩 문제 차단
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
//                .category(product.getCategory())
                .images(product.getImages() != null && !product.getImages().isEmpty() ?
                        product.getImages().stream()
                                .map(ImageDto::fromImage)
                                .toList()
                : new ArrayList<>())
                .build();
    }
}
