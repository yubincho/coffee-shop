package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import com.example.coffeeOrderService.common.pageHandler.PageResponseDto;
import com.example.coffeeOrderService.dto.ProductDto;
import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.category.CategoryRepository;
import com.example.coffeeOrderService.model.orderItem.OrderItemRepository;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.service.product.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest
@Transactional
class ProductServiceTest {

//    @Autowired
//    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ObjectMapper objectMapper;  // JSON 변환에 사용


    @BeforeEach
    void deleteProduct() {
        // 자식 엔티티(OrderItem)부터 삭제
        orderItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    @DisplayName("모든 상품 보여준다.- 커서 로직")
    @Test
    void getAllProducts() throws Exception {
        Category category1 = new Category();
        category1.setName("Gadgets");
        categoryRepository.save(category1);

        Product product1 = Product.builder()
                .name("Product1")
                .description("Description1")
                .brand("Brand1")
                .price(BigDecimal.valueOf(250))
                .inventory(100)
                .category(category1)
                .build();
        product1.setId(1L);
        productRepository.saveAndFlush(product1);

        Product product2 = Product.builder()
                .name("Product2")
                .description("Description2")
                .brand("Brand2")
                .price(BigDecimal.valueOf(350))
                .inventory(300)
                .category(category1)
                .build();
        product2.setId(2L);
        productRepository.saveAndFlush(product2);
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        PageRequestDto pageRequestDto = PageRequestDto.builder()
                .cursor(1L).size(2).keyword(null).build();

        List<Product> products = Arrays.asList(product1, product2);

        System.out.println(products);
        System.out.println("productRepository.findAll()" + productRepository.findAll());

        Page<Product> result = productRepository.searchProducts(pageRequestDto);
        System.out.println("searchProducts : " + productRepository.searchProducts(pageRequestDto));
        System.out.println("[result] " + result);

        List<ProductDto> productDtos = result.stream()
                .map(i -> productService.convertToDto(i)).toList();

        PageResponseDto<ProductDto> resultList = new PageResponseDto<>(productDtos);

        System.out.println("resultList : " + resultList);

//        PageResponseDto<ProductDto> responseDto = productService.getList(pageRequestDto);

//        System.out.println(productService.getList(pageRequestDto));

//        System.out.println(responseDto);

//        assertThat(responseDto.getData().size()).isEqualTo(2);
//        assertThat(responseDto.getNextCursor()).isEqualTo(null);

    }



}