package com.example.coffeeOrderService.domain.order.dto;

import com.example.coffeeOrderService.domain.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusCache implements Serializable {
    private Long orderId;
    private String orderStatus;    // PENDING / CONFIRMED ...
    private Boolean paymentStatus;
    private String payMethod;
    private String merchantUid;
    private String impUid;

    public static OrderStatusCache from(Order order) {
        return OrderStatusCache.builder()
                .orderId(order.getOrderId())
                .orderStatus(String.valueOf(order.getOrderStatus()))
                .paymentStatus(order.getPaymentStatus())
                .payMethod(order.getPayMethod() != null ? order.getPayMethod().name() : null)
                .merchantUid(order.getMerchantUid())
                .impUid(order.getImpUid())
                .build();
    }
}

