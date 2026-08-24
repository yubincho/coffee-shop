package com.example.coffeeOrderService.domain.orderItem.entity;

import com.example.coffeeOrderService.domain.order.entity.Order;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private BigDecimal price;

    /** *************************************************/

    @JsonIgnore   // Order 순환 방지
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @JsonIgnore   // Product 순환 방지
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true) // nullable = true로 설정)
    private Product product;

    @Builder
    public OrderItem(int quantity, BigDecimal price, Order order, Product product) {
        this.quantity = quantity;
        this.price = price;
        this.order = order;
        this.product = product;
    }

    public OrderItem(Order order, Product product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;

    }
}
