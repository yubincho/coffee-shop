package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.dto.ProductDto;
import com.example.coffeeOrderService.exception.AlreadyExistsException;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.request.AddProductRequest;
import com.example.coffeeOrderService.request.UpdateProductRequest;
import com.example.coffeeOrderService.response.ApiResponse;
import com.example.coffeeOrderService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {

    private final ProductService productService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = products.stream()
                .map(productService::convertToDto)
                .toList();
        return ResponseEntity.ok(new ApiResponse("All products successfully", productDtos));
    }


    @GetMapping("/product/{id}/product")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable long id) {
        try {
            Product product = productService.getProductById(id);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product successfully", productDto));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest request) {
        try {
            Product product = productService.addProduct(request);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product added successfully", productDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/product/{id}/update")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable long id, @RequestBody UpdateProductRequest request) {
        try {
            Product product = productService.updateProduct(id, request);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product updated successfully", productDto));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @DeleteMapping("/product/{id}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }


}
