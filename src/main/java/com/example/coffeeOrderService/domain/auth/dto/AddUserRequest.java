package com.example.coffeeOrderService.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AddUserRequest {
    private String email;
    private String password;
    private String passwordCheck;
}
