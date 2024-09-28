package com.example.coffeeOrderService.request.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


// View로 전달할 결제 관련 데이터
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RequestPayment {

    private Long orderId;   // 주문 ID
    private String itemName; // 주문된 상품명
    private BigDecimal paymentPrice; // 결제된 금액
    private String userEmail;  // 사용자 이메일
    private String impUid;  // 결제 고유 식별자 (imp_uid)
    private String buyerName;  // 구매자 이름
    private String buyerAddress;  // 구매자 주소



    @Builder
    public RequestPayment(Long orderId, String itemName,
                          BigDecimal paymentPrice, String userEmail,
                          String impUid, String buyerName, String buyerAddress) {

        this.orderId = orderId;
        this.itemName = itemName;
        this.paymentPrice = paymentPrice;
        this.userEmail = userEmail;
        this.impUid = impUid;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
    }
}
