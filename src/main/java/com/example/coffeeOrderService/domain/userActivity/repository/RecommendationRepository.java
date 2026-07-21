package com.example.coffeeOrderService.domain.userActivity.repository;

import com.example.coffeeOrderService.domain.userActivity.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // 특정 사용자의 추천 목록을 조회하는 메서드
    List<Recommendation> findByUserId(Long userId);
}
