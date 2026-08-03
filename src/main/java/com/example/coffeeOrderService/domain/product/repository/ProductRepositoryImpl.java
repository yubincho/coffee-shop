package com.example.coffeeOrderService.domain.product.repository;

import com.example.coffeeOrderService.common.dto.pageHandler.ScrollPaginationResult;
import com.example.coffeeOrderService.common.dto.pageHandler.dto.PageRequestDto;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.entity.QProduct;
import com.example.coffeeOrderService.domain.product.dto.PriceRangeDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import lombok.extern.slf4j.Slf4j;

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
    public ScrollPaginationResult<Product> searchProducts(PageRequestDto pageRequestDto) {

        QProduct qProduct = QProduct.product;

        Long cursor = pageRequestDto.getCursor();  // 커서 값 (마지막 조회된 Product의 ID)
        int pageSize = (pageRequestDto.getSize() != null) ? pageRequestDto.getSize() : 10; // 페이지 크기

        BooleanExpression searchCondition = getSearchCondition(qProduct, pageRequestDto)
                .and(qProduct.deleted.isFalse()); // 논리 삭제되지 않은 상품만 조회

        // 커서가 있으면 커서보다 큰 ID의 제품만 조회
        if (cursor != null) {
            searchCondition = searchCondition.and(qProduct.id.gt(cursor));
        }

        // 1단계: 커서 페이징으로 '상품 ID만' 먼저 조회 (limit 정확성 보장)
        List<Long> productIds = queryFactory
                .select(qProduct.id)
                .from(qProduct)
                .where(searchCondition)
                .orderBy(qProduct.id.asc())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = productIds.size() > pageSize;
        if (hasNext) {
            productIds = productIds.subList(0, pageSize);  // 여분 제거
        }

        // 2단계: 그 ID들로 images를 fetch join 해서 실제 데이터 조회
        List<Product> products = queryFactory
                .selectFrom(qProduct)
                .leftJoin(qProduct.images).fetchJoin()
                .where(qProduct.id.in(productIds))
                .orderBy(qProduct.id.asc())
                .fetch();

        // 커서 페이징이므로 totalCount 없이 상품 리스트 + hasNext만 반환
        return new ScrollPaginationResult<>(products, hasNext);
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
