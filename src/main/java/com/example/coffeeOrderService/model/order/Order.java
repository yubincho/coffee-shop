package com.example.coffeeOrderService.model.order;

import com.example.coffeeOrderService.model.payment.PaymentHistory;
import com.example.coffeeOrderService.model.orderItem.OrderItem;
import com.example.coffeeOrderService.model.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    private LocalDate orderDate;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_method", nullable = true)
    private PayMethod payMethod; // 결제 방식

    @Column(length = 100, name = "merchant_uid", nullable = true)
    private String merchantUid; // 주문번호

    @Column(name = "imp_uid", nullable = true)
    private String impUid;  // 결제 고유 식별자 (imp_uid)

    @Column(name = "payment_status")
    private Boolean paymentStatus = false; // 결제 상태

    @OneToMany(mappedBy = "order")
    private List<PaymentHistory> paymentHistories = new ArrayList<>(); // 결제내역과 일대다

    // 주문을 확정하는 메서드
    public void confirmOrder() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    // 결제 완료 시 결제 식별자 업데이트
    public void setPaymentCompleted(String impUid) {
        this.paymentStatus = true;
        this.impUid = impUid;  // imp_uid 저장
    }

    /** *************************************************/

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 여기가 문제일 가능성 있음
    private User user;

    @Builder
    public Order(LocalDate orderDate, OrderStatus orderStatus, User user) {
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.user = user;
    }
}
