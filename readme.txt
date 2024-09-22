

- Product가 업데이트, 되지 않고 신규 생성되는 현상 발생

[ 문제의 코드 ]
public Product updateProduct(long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> renewProduct(request))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private Product renewProduct(UpdateProductRequest request) {
        Product updateProduct = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .price(request.getPrice())
                .inventory(request.getInventory())
                .build();

        Category category = categoryRepository.findByName(request.getCategory().getName());
        updateProduct.setCategory(category);
        return productRepository.save(updateProduct);
    }

[ 문제 원인 ]
Product.builder()를 통해 새로운 Product 객체를 생성하고 있다.
그래서 기존의 제품을 수정하는 것이 아니라, 새로 생성된 Product가 저장되는 것이다.

[ 해결 방법 ]
기존 **existingProduct**를 업데이트해야 한다.
즉, Product.builder()로 새로운 객체를 만들지 말고, 기존 객체의 필드만 업데이트해야 한다.

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
