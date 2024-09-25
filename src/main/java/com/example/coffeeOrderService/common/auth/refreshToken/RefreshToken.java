package com.example.coffeeOrderService.common.auth.refreshToken;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@NoArgsConstructor
@Getter
@Setter
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Instant expiryDate;


}
