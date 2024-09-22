package com.example.coffeeOrderService.model.product;


import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.image.Image;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;


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
