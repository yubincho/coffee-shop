package com.example.coffeeOrderService.domain.auth.refreshToken;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;
}
