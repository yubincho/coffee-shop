package com.example.coffeeOrderService.domain.product.service;

import com.example.coffeeOrderService.common.exception.AlreadyExistsException;
import com.example.coffeeOrderService.domain.category.entity.Category;
import com.example.coffeeOrderService.domain.category.repository.CategoryRepository;
import com.example.coffeeOrderService.domain.product.dto.AddProductRequest;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.extension.ExtendWith;   // ← @ExtendWith
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;    // ← MockitoExtension

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;           // given(...)

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest2 {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;


    private AddProductRequest createRequest() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setPrice(BigDecimal.valueOf(1500000));
        request.setInventory(10);
        request.setDescription("최신 아이폰");
        request.setCategory(new Category("Electronics"));  // 카테고리도 세팅
        return request;
    }

    @Test
    void addProduct_이미_존재하는_상품이면_예외() {
        AddProductRequest request = createRequest();

        given(productRepository.existsByNameAndBrand("iPhone 15", "Apple"))
                .willReturn(true);

        assertThatThrownBy(() -> productService.addProduct(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Product already exists");

        verify(categoryRepository, never()).findByName(anyString());

    }

    @Test
    void addProduct_카테고리가_이미_있으면_기존_카테고리_사용() {
        AddProductRequest request = createRequest();
        Category existingCategory = new Category("Electronics");

        given(productRepository.existsByNameAndBrand("iPhone 15", "Apple"))
                .willReturn(false);
        given(categoryRepository.findByName("Electronics"))
                .willReturn(existingCategory);
        given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.addProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void addProduct_카테고리가_없으면_새로_저장() {
        AddProductRequest request = createRequest();

        given(productRepository.existsByNameAndBrand(anyString(), anyString()))
                .willReturn(false);
        given(categoryRepository.findByName("Electronics"))
                .willReturn(null);
        given(categoryRepository.save(any(Category.class)))
                .willReturn(new Category("Electronics"));
        given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.addProduct(request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));


    }





}