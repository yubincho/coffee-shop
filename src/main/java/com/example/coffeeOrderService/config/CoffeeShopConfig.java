package com.example.coffeeOrderService.config;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@RequiredArgsConstructor
@Configuration
public class CoffeeShopConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
