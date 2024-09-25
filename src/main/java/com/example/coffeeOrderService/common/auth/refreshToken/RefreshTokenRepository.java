package com.example.coffeeOrderService.common.auth.refreshToken;

import com.example.coffeeOrderService.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByRefreshToken(String token);

    void deleteByUserId(Long userId);

}
