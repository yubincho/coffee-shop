package com.example.coffeeOrderService.domain.payment.dto;

import lombok.Data;


@Data
public class RequestOrder {

    private Long orderId;  // 클라이언트가 보낸 주문 ID
    private String address;
    private String username;
    private String payMethod;  // kakaopay

}
