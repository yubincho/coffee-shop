package com.example.coffeeOrderService.model.user.recommandation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * (사용자에게 제공할) 추천 데이터를 저장하고 관리
 * */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "recommendation")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 추천을 받은 사용자 ID

    private Long productId; // 추천된 제품 ID

    private LocalDateTime recommendedAt; // 추천 생성 시각


    public Recommendation(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
        this.recommendedAt = LocalDateTime.now(); // 추천 생성 시각
    }
}
