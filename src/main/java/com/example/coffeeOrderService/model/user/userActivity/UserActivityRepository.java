package com.example.coffeeOrderService.model.user.userActivity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    // 특정 사용자의 활동 기록을 가져오는 메서드
    List<UserActivity> findByUserId(Long userId);

    // 특정 활동 유형을 기준으로 데이터를 조회하는 메서드 (예: ORDER_PLACED)
    List<UserActivity> findByUserIdAndAction(Long userId, String action);
}
