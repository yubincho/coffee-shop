package com.example.coffeeOrderService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
public class PriceRangeDto {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public PriceRangeDto(BigDecimal minPrice, BigDecimal maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
