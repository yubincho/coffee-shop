package com.example.coffeeOrderService.model.product;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import org.springframework.data.domain.Page;


public interface ProductRepositoryCustom {
    Page<Product> searchProducts(PageRequestDto pageRequestDto);
}
