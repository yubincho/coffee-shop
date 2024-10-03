package com.example.coffeeOrderService.model.user.userActivity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User 의 구매 패턴, 사이트 이용 패턴 데이터를 관리
 * */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_activity")
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 사용자 ID

    private String action; // 활동 종류 (ADD_TO_CART, ORDER_PLACED 등)

    private Long productId; // 해당 활동과 관련된 제품 ID (장바구니 추가, 구매 등)

    private Long orderId; // 주문 완료 시 관련된 주문 ID (선택 사항)

    private LocalDateTime timestamp; // 활동이 발생한 시간

    public UserActivity(Long userId, String action, Long productId) {
        this.userId = userId;
        this.action = action;
        this.productId = productId;
        this.timestamp = LocalDateTime.now();
    }
}
