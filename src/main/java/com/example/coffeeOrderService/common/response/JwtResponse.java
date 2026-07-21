package com.example.coffeeOrderService.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private Long id;
    private String token;
}
