package com.example.coffeeOrderService.domain.product.repository.document;


import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;

import com.example.coffeeOrderService.domain.product.entity.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class ProductSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    // 키워드로 검색해 상품 ID만 커서 페이징으로 반환 (productId 오름차순, size+1개)
    public List<Long> searchProductIds(String keyword, Long cursor, int size) {

        Query boolQuery = Query.of(q -> q
                .bool(b -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        b.must(m -> m
                                .multiMatch(mm -> mm
                                        .query(keyword)
                                        .fields("name^2", "description")  // name 가중치 2배
                                )
                        );
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }

                    if (cursor != null) {
                        b.filter(f -> f
                                .range(r -> r.field("productId").gt(JsonData.of(cursor)))
                        );
                    }
                    return b;
                })
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withSort(s -> s.field(f -> f.field("productId").order(SortOrder.Asc)))
                .withPageable(PageRequest.of(0, size + 1))
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getProductId())
                .toList();
    }
}
