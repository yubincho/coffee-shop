package com.example.coffeeOrderService.domain.product.repository.document;

import com.example.coffeeOrderService.domain.product.entity.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, Long> {
}
