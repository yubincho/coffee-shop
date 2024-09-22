package com.example.coffeeOrderService.service.product;

import com.example.coffeeOrderService.dto.ImageDto;
import com.example.coffeeOrderService.dto.ProductDto;
import com.example.coffeeOrderService.exception.AlreadyExistsException;
import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.category.CategoryRepository;
import com.example.coffeeOrderService.model.image.Image;
import com.example.coffeeOrderService.model.image.ImageRepository;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.request.AddProductRequest;

import com.example.coffeeOrderService.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class ProductService {

    public final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;


    public Product addProduct(AddProductRequest request) {
        if (isProductExists(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException("Product already exists");
        }
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> categoryRepository.save(new Category(request.getCategory().getName())));

        request.setCategory(category);
        return createProduct(request, category);
    }

    private boolean isProductExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return productRepository.save(Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .brand(request.getBrand())
                .inventory(request.getInventory())
                .category(category)
                .build());
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public Product getProductById(long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );
    }


    public Product updateProduct(long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // 기존의 product를 업데이트
                    existingProduct.setName(request.getName());
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setBrand(request.getBrand());
                    existingProduct.setPrice(request.getPrice());
                    existingProduct.setInventory(request.getInventory());

                    // 카테고리도 업데이트
                    Category category = categoryRepository.findByName(request.getCategory().getName());
                    existingProduct.setCategory(category);

                    // 수정된 기존 product를 저장
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }


    public void deleteProduct(long id) {
        Product oldProduct = getProductById(id);
        if (oldProduct != null) {
            productRepository.delete(oldProduct);
        } else {
            throw new ResourceNotFoundException("Product not found!");
        }
    }



    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        productDto.setImages(imageDtos);
        return productDto;
    }


    public Product findById(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found")
        );
    }
}
