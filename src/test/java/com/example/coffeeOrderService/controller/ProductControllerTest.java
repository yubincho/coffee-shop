package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.common.pageHandler.PageResponseDto;
import com.example.coffeeOrderService.dto.ProductDto;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.service.product.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
class ProductControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;  // JSON 변환에 사용

    private List<ProductDto> productDtos;


    @BeforeEach
    void setUp() {
        productDtos = Arrays.asList(
                new ProductDto(1L, "Product 1", "Samsung", new BigDecimal("100.00"), 50, "AVAILABLE", "Description 1", null, null),
                new ProductDto(2L, "Product 2", "Apple", new BigDecimal("200.00"), 30, "AVAILABLE","Description 2", null, null)
        );
    }


    @DisplayName("페이징 적용하여 모든 상품 가져오기")
    @Test
    void getAllProducts() throws Exception {

//        List<ProductDto> productDtoList = productDtos;
//        when(productService.getAllProductDtos()).thenReturn((Page<ProductDto>) productDtoList);

        // 페이징된 결과를 반환하는 PageImpl 객체로 Mock 데이터 설정
        Page<ProductDto> pageResult = new PageImpl<>(productDtos, PageRequest.of(0, 10), productDtos.size());

        // 서비스가 PageResponseDto<ProductDto>를 반환하도록 Mock 설정
        PageResponseDto<ProductDto> pageResponseDto = new PageResponseDto<>(pageResult);
        when(productService.getList(Mockito.any())).thenReturn(pageResponseDto);

        mockMvc.perform(get("/api/v1/products/all")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.dtoList", hasSize(2)))
                .andExpect(jsonPath("$.data.dtoList[0].price").value(100.00))
                .andExpect(jsonPath("$.data.dtoList[1].price").value(200.00));

    }

    @DisplayName("keyword 검색하여 (페이징된) 모든 상품 가져오기")
    @Test
    void getAllProductsWithKeyword() throws Exception {

        Page<ProductDto> pageResult = new PageImpl<>(productDtos, PageRequest.of(0, 10), productDtos.size());

        PageResponseDto<ProductDto> pageResponseDto = new PageResponseDto<>(pageResult);

        when(productService.getList(Mockito.any())).thenReturn(pageResponseDto);

        mockMvc.perform(get("/api/v1/products/all")
                        .param("page", "1")
                        .param("size", "10")
//                        .param("type", "t")  // 검색 타입: 상품 이름
                        .param("keyword", "Samsung")  // 검색어: "Samsung"
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // HTTP 200 상태코드 확인
                .andExpect(jsonPath("$.message").value("Success"))  // 성공 메시지 확인
                .andExpect(jsonPath("$.data.dtoList", hasSize(2)))  // 검색 결과로 반환된 상품 리스트 크기 확인
                .andExpect(jsonPath("$.data.dtoList[0].name").value("Product 1"))
                .andExpect(jsonPath("$.data.dtoList[0].brand").value("Samsung"))
                .andExpect(jsonPath("$.data.dtoList[1].name").value("Product 2"))
                .andExpect(jsonPath("$.data.dtoList[1].brand").value("Apple"));
    }


    @DisplayName("첫 번째 페이지에서 2개의 데이터를 정상적으로 가져오기")
    @Test
    void getFirstPageWithTwoItems() throws Exception {
        Page<ProductDto> pageResult = new PageImpl<>(productDtos, PageRequest.of(0, 10), productDtos.size());

        PageResponseDto<ProductDto> pageResponseDto = new PageResponseDto<>(pageResult);

        when(productService.getList(Mockito.any())).thenReturn(pageResponseDto);

        // 첫 번째 페이지 요청 (page=1, size=2)
        mockMvc.perform(get("/api/v1/products/all")
                        .param("page", "1")  // 1번째 페이지 (PageRequest는 0 기반)
                        .param("size", "2")  // 페이지당 2개 데이터
//                        .param("type", "t")  // 검색 타입
                        .param("keyword", "Product")  // 검색어: "Product"
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // HTTP 200 상태코드 확인
                .andExpect(jsonPath("$.data.dtoList", hasSize(2)));
//                .andExpect(jsonPath("$.data.dtoList[0].name").value("Product 1"))
//                .andExpect(jsonPath("$.data.dtoList[1].name").value("Product 2"));
    }


    @DisplayName("두 번째 페이지에서 빈 데이터를 가져오기")
    @Test
    void getSecondPageWithNoItems() throws Exception {

        // 두 번째 페이지 (빈 결과)
        Page<ProductDto> emptyPageResult = new PageImpl<>(new ArrayList<>(), PageRequest.of(1, 2), 2);

        PageResponseDto<ProductDto> emptyPageResponseDto = new PageResponseDto<>(emptyPageResult);

        when(productService.getList(Mockito.any())).thenReturn(emptyPageResponseDto);

        // 두 번째 페이지 요청 (page=2, size=2)
        mockMvc.perform(get("/api/v1/products/all")
                        .param("page", "2")  // 2번째 페이지 (PageRequest는 0 기반)
                        .param("size", "2")  // 페이지당 2개 데이터
//                        .param("type", "t")  // 검색 타입
                        .param("keyword", "Product")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // HTTP 200 상태코드 확인
//                .andExpect(jsonPath("$.data.dtoList", hasSize(0)))  // 빈 리스트인지 확인
                .andExpect(jsonPath("$.data.next").value(false));
    }



}
