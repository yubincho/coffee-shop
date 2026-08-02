package com.example.coffeeOrderService.domain.product.service;


import com.example.coffeeOrderService.common.exception.AlreadyExistsException;
import com.example.coffeeOrderService.domain.category.entity.Category;
import com.example.coffeeOrderService.domain.category.repository.CategoryRepository;
import com.example.coffeeOrderService.domain.product.dto.AddProductRequest;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
@Transactional   // 각 테스트 끝나면 롤백 → DB 깨끗하게 유지 (중요!)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;   // 목 아님! 진짜 빈 주입

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private AddProductRequest createRequest() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setPrice(BigDecimal.valueOf(1500000));
        request.setInventory(10);
        request.setDescription("최신 아이폰");
        request.setCategory(new Category("Electronics"));
        return request;
    }


    @Test
    void addProduct_정상적으로_저장된다() {
        AddProductRequest request = createRequest();

        Product savedProduct = productService.addProduct(request);

        assertThat(savedProduct.getId()).isNotNull();   // 실제 ID가 발급됨
        assertThat(savedProduct.getName()).isEqualTo("iPhone 15");
        assertThat(savedProduct.getBrand()).isEqualTo("Apple");
    }


    @Test
    void addProduct_이미_존재하는_상품이면_예외() {
        productService.addProduct(createRequest());

        AddProductRequest duplicate = createRequest();

        assertThatThrownBy(() -> productService.addProduct(duplicate))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Product already exists");
    }


    @Test
    void addProduct_없는_카테고리는_새로_생성된다() {
        AddProductRequest request = createRequest();

        Product savedProduct = productService.addProduct(request);

        Category category = categoryRepository.findByName("Electronics");
        assertThat(category).isNotNull();
        assertThat(savedProduct.getCategory().getName()).isEqualTo("Electronics");
    }



}
