package com.example.coffeeOrderService.domain.product.entity.document;


import com.example.coffeeOrderService.domain.product.entity.Product;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")   // ES 인덱스 이름
public class ProductDocument {

    @Id
    private Long id;                     // id는 ES 문서 식별용

    @Field(type = FieldType.Long)        // productId는 정렬/범위 검색용 (커서)
    private Long productId;

    // 상품명: nori 분석기로 한글 형태소 검색
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    // 설명: 역시 nori로 검색 가능하게
    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    // 브랜드: 정확 매칭/필터용 (분석 안 함)
    @Field(type = FieldType.Keyword)
    private String brand;

    // 가격: 정렬/범위 검색용
    @Field(type = FieldType.Double)
    private BigDecimal price;

    // MySQL Product를 ES Document로 변환
    public static ProductDocument from(Product product) {
        return ProductDocument.builder()
                .id(product.getId())
                .productId(product.getId())  // id와 동일하게 채움
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getPrice())
                .build();
    }
}
