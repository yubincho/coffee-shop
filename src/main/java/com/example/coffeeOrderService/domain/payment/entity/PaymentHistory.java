package com.example.coffeeOrderService.domain.payment.entity;


import com.example.coffeeOrderService.domain.order.entity.Order;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "payment_history")
@Entity
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long id;

    private String paymentUid; // 결제 고유 번호

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "paid_at", nullable = false)
    private LocalDate paidAt;

    @Column(name = "status")
    private Boolean paymentStatus = true;

    /** ********************************************************************/

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // 주문 테이블과 다대일 (연관관계 주인은 주문)

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    public PaymentHistory(User user, Order order,
                          Product product, String paymentUid,
                          BigDecimal totalPrice, LocalDate paidAt, boolean paymentStatus) {
        this.user = user;
        this.order = order;
        this.product = product;
        this.paymentUid = paymentUid;
        this.totalPrice = totalPrice;
        this.paidAt = paidAt;
        this.paymentStatus = paymentStatus;
    }
}
