package com.example.coffeeOrderService.model.user.userActivity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserActivity is a Querydsl query type for UserActivity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserActivity extends EntityPathBase<UserActivity> {

    private static final long serialVersionUID = 1657362234L;

    public static final QUserActivity userActivity = new QUserActivity("userActivity");

    public final StringPath action = createString("action");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> timestamp = createDateTime("timestamp", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserActivity(String variable) {
        super(UserActivity.class, forVariable(variable));
    }

    public QUserActivity(Path<? extends UserActivity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserActivity(PathMetadata metadata) {
        super(UserActivity.class, metadata);
    }

}

