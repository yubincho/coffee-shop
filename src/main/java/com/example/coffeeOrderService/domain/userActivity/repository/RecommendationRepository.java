package com.example.coffeeOrderService.domain.userActivity.repository;

import com.example.coffeeOrderService.domain.userActivity.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // 특정 사용자의 추천 목록을 조회하는 메서드
    List<Recommendation> findByUserId(Long userId);

    @Modifying  // 이건 조회가 아니라 데이터를 변경(삭제)하는 쿼리
    @Transactional
    void deleteByUserId(Long userId);
}
