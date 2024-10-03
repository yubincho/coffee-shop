package com.example.coffeeOrderService.model.user.recommandation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecommendation is a Querydsl query type for Recommendation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecommendation extends EntityPathBase<Recommendation> {

    private static final long serialVersionUID = -571944618L;

    public static final QRecommendation recommendation = new QRecommendation("recommendation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> recommendedAt = createDateTime("recommendedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QRecommendation(String variable) {
        super(Recommendation.class, forVariable(variable));
    }

    public QRecommendation(Path<? extends Recommendation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecommendation(PathMetadata metadata) {
        super(Recommendation.class, metadata);
    }

}

