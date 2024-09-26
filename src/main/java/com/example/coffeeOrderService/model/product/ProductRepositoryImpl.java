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
        long total = queryFactory
                .select(qProduct.id)  // 총 개수는 id만 세면 충분함
                .from(qProduct)
                .where(searchCondition)
                .fetch()
                .size();

        return new PageImpl<>(products, pageable, total);
    }

    // 동적 검색 조건 생성 메서드
    private BooleanExpression getSearchCondition(QProduct qProduct, PageRequestDto pageRequestDto) {
        String type = pageRequestDto.getType();
        String keyword = pageRequestDto.getKeyword();

        // 기본 조건: id > 0 (모든 제품을 조회하는 기본 조건)
        BooleanExpression expression = qProduct.id.gt(0L);

        // 검색 조건을 추가하기 전에 type과 keyword가 null이거나 비어있는지 체크
        if ((type == null || type.isEmpty()) && (keyword == null || keyword.trim().isEmpty())) {
            // 검색 조건이 없으면 기본 조건만 반환
            return expression;
        }

        BooleanExpression searchCondition = null;

        // 키워드가 있을 경우에만 검색 조건 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (type != null && type.contains("t")) {
                searchCondition = qProduct.name.containsIgnoreCase(keyword);
            }
            if (type != null && type.contains("c")) {
                searchCondition = (searchCondition == null) ?
                        qProduct.description.containsIgnoreCase(keyword) :
                        searchCondition.or(qProduct.description.containsIgnoreCase(keyword));
            }
            if (type != null && type.contains("w")) {
                searchCondition = (searchCondition == null) ?
                        qProduct.brand.containsIgnoreCase(keyword) :
                        searchCondition.or(qProduct.brand.containsIgnoreCase(keyword));
            }
        }

        // 기본 조건과 검색 조건 결합
        return (searchCondition == null) ? expression : expression.and(searchCondition);
    }


}
