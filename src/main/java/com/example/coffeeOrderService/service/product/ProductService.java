package com.example.coffeeOrderService.service.product;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import com.example.coffeeOrderService.common.pageHandler.PageResponseDto;
import com.example.coffeeOrderService.dto.ImageDto;
import com.example.coffeeOrderService.dto.ProductDto;
import com.example.coffeeOrderService.common.exception.AlreadyExistsException;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.category.Category;
import com.example.coffeeOrderService.model.category.CategoryRepository;
import com.example.coffeeOrderService.model.image.Image;
import com.example.coffeeOrderService.model.image.ImageRepository;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.model.product.QProduct;
import com.example.coffeeOrderService.request.AddProductRequest;

import com.example.coffeeOrderService.request.UpdateProductRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<ProductDto> getAllProductDtos(Pageable pageable) {
        Page<Product> productPage = getAllProducts(pageable);  // 실제 Product 목록 가져오기
        return productPage.map(this::convertToDto);  // Product를 ProductDto로 변환하는 메서드
    }


    // 페이징 + 검색 적용
    public PageResponseDto<ProductDto> getList(PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.getPageable(Sort.by("id").descending());

        BooleanBuilder booleanBuilder = getSearch(pageRequestDto);

        Page<Product> result = productRepository.findAll(booleanBuilder, pageable);
        Page<ProductDto> dtoPage = result.map(this::convertToDto);
        return new PageResponseDto<>(dtoPage);
    }

    private BooleanBuilder getSearch(PageRequestDto pageRequestDto) {
        String type = pageRequestDto.getType();
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QProduct qProduct = QProduct.product;
        String keyword = pageRequestDto.getKeyword();
        BooleanExpression expression = qProduct.id.gt(0L);  // id > 0 조건만
        booleanBuilder.and(expression);

        if (type == null || type.isEmpty()) {
            return booleanBuilder;
        }

        BooleanBuilder conditionBuilder = new BooleanBuilder();

        if(type.contains("t")){
            conditionBuilder.or(qProduct.name.contains(keyword));
        }
        if(type.contains("c")){
            conditionBuilder.or(qProduct.description.contains(keyword));
        }
        if(type.contains("w")){
            conditionBuilder.or(qProduct.brand.contains(keyword));
        }

        booleanBuilder.and(conditionBuilder);
        return booleanBuilder;
    }


//    public List<ProductDto> getAllProductDtos() {
//        List<Product> products = getAllProducts();  // 실제 Product 목록 가져오기
//        return products.stream()
//                .map(this::convertToDto)  // Product를 ProductDto로 변환하는 메서드
//                .collect(Collectors.toList());
//    }


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
