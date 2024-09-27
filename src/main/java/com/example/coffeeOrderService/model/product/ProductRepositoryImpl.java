package com.example.coffeeOrderService.model.product;

import com.example.coffeeOrderService.common.pageHandler.PageRequestDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;


public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }


    @Override
    public Page<Product> searchProducts(PageRequestDto pageRequestDto) {

        QProduct qProduct = QProduct.product;

        Pageable pageable = pageRequestDto.getPageable(Sort.by("id").descending());

        BooleanExpression searchCondition = getSearchCondition(qProduct, pageRequestDto);

        List<Product> products = queryFactory
                .selectFrom(qProduct)
                .where(searchCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 개수 가져오기
        long totalCount = queryFactory
                .select(qProduct.id)  // 총 개수는 id만 세면 충분함
                .from(qProduct)
                .where(searchCondition)
                .fetch()
                .size();

        return new PageImpl<>(products, pageable, totalCount);
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



}
