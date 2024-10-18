package com.example.coffeeOrderService.model.product;


import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.image.Image;
import com.example.coffeeOrderService.model.orderItem.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private int inventory;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;  // 기본값: 판매중 상품으로 설정

    /** *************************************************/

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItem;

    /** *************************************************/

    // 논리 삭제를 위한 플래그
    private boolean deleted = false;

    // 삭제 메서드
    public void delete() {
        this.deleted = true;
    }


    @Builder
    public Product(String name, String description, String brand,
                   BigDecimal price, int inventory, Category category) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.category = category;
    }


    public Product(String name, String brand, BigDecimal price,
                   int inventory, String description) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.description = description;
    }
}
