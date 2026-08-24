package com.example.coffeeOrderService.domain.product.entity;


import com.example.coffeeOrderService.common.exception.OutOfStockException;
import com.example.coffeeOrderService.domain.category.entity.Category;
import com.example.coffeeOrderService.domain.image.entity.Image;
import com.example.coffeeOrderService.domain.orderItem.entity.OrderItem;
import com.example.coffeeOrderService.domain.product.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // ← 명시한 것만 포함
@Getter
@Setter
@ToString(exclude = {"category", "images", "orderItem"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include  // ← id만 포함!
    private Long id;

    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private int inventory;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;  // 기본값: 판매중 상품으로 설정

    // 낙관적 락
    @Version
    private Long version;

    /** *************************************************/

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @JsonIgnore
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

    // 재고 차감
    public void removeStock(int quantity) {
        int restStock = this.inventory - quantity;
        if (restStock < 0) {
            throw new OutOfStockException(
                    "재고가 부족합니다. 현재 재고: " + this.inventory + ", 요청 수량: " + quantity);
        }
        this.inventory = restStock;
    }

    public void addStock(int quantity) {   // 주문 취소 등으로 재고 되돌릴 때
        this.inventory += quantity;
    }

}
