package com.example.coffeeOrderService.domain.product.service.document;

import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.entity.document.ProductDocument;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import com.example.coffeeOrderService.domain.product.repository.document.ProductSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class ProductIndexService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    private static final int BATCH_SIZE = 1000;

    // MySQL의 삭제되지 않은 상품 전체를 ES로 색인 (초기/재색인용)
    public long reindexAll() {
        // 깨끗하게 다시 넣기 위해 기존 인덱스 데이터 비우기
        productSearchRepository.deleteAll();

        long totalIndexed = 0;
        int pageNumber = 0;
        Page<Product> page;

        do {
            // ID 오름차순으로 페이지 단위 조회 (deleted = false만)
            page = productRepository.findByDeletedFalse(
                    PageRequest.of(pageNumber, BATCH_SIZE));

            List<ProductDocument> documents = page.getContent().stream()
                    .map(ProductDocument::from)
                    .toList();

            if (!documents.isEmpty()) {
                productSearchRepository.saveAll(documents);  // bulk 저장
                totalIndexed += documents.size();
                log.info("색인 진행: {}건 완료", totalIndexed);
            }

            pageNumber++;
        } while (page.hasNext());

        log.info("색인 전체 완료: 총 {}건", totalIndexed);
        return totalIndexed;
    }
}