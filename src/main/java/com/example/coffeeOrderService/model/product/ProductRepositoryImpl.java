package com.example.coffeeOrderService.model.product;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import com.example.coffeeOrderService.dto.PriceRangeDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }


    @Override
    public Page<Product> searchProducts(PageRequestDto pageRequestDto) {

        QProduct qProduct = QProduct.product;

        Long cursor = pageRequestDto.getCursor();  // 커서 값 (마지막 조회된 Product의 ID)
        int pageSize = (pageRequestDto.getSize() != null) ? pageRequestDto.getSize() : 10; // 페이지 크기

        BooleanExpression searchCondition = getSearchCondition(qProduct, pageRequestDto)
                .and(qProduct.deleted.isFalse()); // 논리 삭제되지 않은 상품만 조회

        // 커서가 있으면 커서보다 큰 ID의 제품만 조회
        if (cursor != null) {
            searchCondition = searchCondition.and(qProduct.id.gt(cursor));
        }

        // 커서를 기반으로 다음 페이지 데이터를 조회
        List<Product> products = queryFactory
                .selectFrom(qProduct)
                .where(searchCondition)
                .orderBy(qProduct.id.asc())  // 커서 페이징은 보통 ID 기준으로 정렬
                .limit(pageSize)             // 페이지 크기 만큼 제한
                .fetch();


        // 총 개수 가져오기
        long totalCount = queryFactory
                .select(qProduct.id)  // 총 개수는 id만 세면 충분함
                .from(qProduct)
                .where(searchCondition)
                .fetch()
                .size();

        // PageImpl을 사용해 List<Product>를 Page<Product>로 변환
        // 커서 기반 페이징이므로 PageRequest에서 오프셋은 0으로 고정, 페이지 크기만 설정
        // 커서 기반 페이징에서는 페이지 번호 자체는 필요 으므로 이렇게 처리함
        return new PageImpl<>(products, PageRequest.of(0, pageSize), totalCount);
    }

    // 동적 검색 조건 생성 메서드
    private BooleanExpression getSearchCondition(QProduct qProduct, PageRequestDto pageRequestDto) {
//        String type = pageRequestDto.getType();
        String keyword = pageRequestDto.getKeyword();

        // 기본 조건: id > 0 (모든 제품을 조회하는 기본 조건)
        BooleanExpression expression = qProduct.id.gt(0L);

        // 키워드가 비어있거나 null이 아닌 경우에만 검색 조건 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            BooleanExpression searchCondition = null;

            // 모든 필드에서 검색
            searchCondition = qProduct.name.containsIgnoreCase(keyword)
                    .or(qProduct.description.containsIgnoreCase(keyword))
                    .or(qProduct.brand.containsIgnoreCase(keyword));

            // 기본 조건과 검색 조건 결합
            return expression.and(searchCondition);
        }

        // 검색 조건이 없으면 기본 조건만 반환
        return expression;
    }


    @Override
    public List<Product> findSimilarProducts(Long categoryId, BigDecimal minPrice,
                                             BigDecimal maxPrice, Long productId) {
        QProduct qProduct = QProduct.product;

        // minPrice와 maxPrice가 null인지 확인
        if (minPrice == null || maxPrice == null) {
            log.error("Price range is null. minPrice: {}, maxPrice: {}", minPrice, maxPrice);
            return new ArrayList<>();  // 빈 리스트 반환
        }

        return queryFactory
                .selectFrom(qProduct)
                .where(
                        qProduct.category.id.eq(categoryId)
                                .and(qProduct.brand.eq(qProduct.brand))
                                .and(qProduct.price.between(minPrice, maxPrice))
                                .and(qProduct.id.ne(productId))  // 자기 자신은 제외
                )
                .fetch();
    }

    @Override
    public PriceRangeDto findMinPriceAndMaxPrice(Long productId) {
        QProduct product = QProduct.product;

        // productId로 해당 제품을 조회하여 카테고리와 브랜드를 가져옴
        Product targetProduct = queryFactory
                .selectFrom(product)
                .where(product.id.eq(productId))
                .fetchOne();

        if (targetProduct == null) {
            return null; // 해당 제품을 찾지 못한 경우 처리
        }

        Tuple result = (Tuple) queryFactory
                .select(product.price.min(), product.price.max())
                .from(product)
                .where(
                        product.category.eq(targetProduct.getCategory())
                                .and(product.brand.eq(targetProduct.getBrand()))
                                .and(product.id.ne(productId))  // 자기 자신은 제외
                )
                .fetchOne();  // 단일 결과를 반환

        if (result != null) {
            BigDecimal minPrice = result.get(product.price.min());
            BigDecimal maxPrice = result.get(product.price.max());
            return new PriceRangeDto(minPrice, maxPrice);
        }
        return null; // 결과가 없는 경우 처리
    }


}
