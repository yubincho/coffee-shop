package com.example.coffeeOrderService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
public class CoffeeOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeOrderServiceApplication.class, args);
	}
}
