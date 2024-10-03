package com.example.coffeeOrderService.model.product;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import com.example.coffeeOrderService.dto.PriceRangeDto;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;


public interface ProductRepositoryCustom {
    Page<Product> searchProducts(PageRequestDto pageRequestDto);

    List<Product> findSimilarProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Long productId);

    PriceRangeDto findMinPriceAndMaxPrice(Long productId);
}
