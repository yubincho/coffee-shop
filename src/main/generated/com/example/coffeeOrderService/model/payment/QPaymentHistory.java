package com.example.coffeeOrderService.model.payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentHistory is a Querydsl query type for PaymentHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentHistory extends EntityPathBase<PaymentHistory> {

    private static final long serialVersionUID = 1904110021L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentHistory paymentHistory = new QPaymentHistory("paymentHistory");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.coffeeOrderService.model.order.QOrder order;

    public final DatePath<java.time.LocalDate> paidAt = createDate("paidAt", java.time.LocalDate.class);

    public final BooleanPath paymentStatus = createBoolean("paymentStatus");

    public final StringPath paymentUid = createString("paymentUid");

    public final com.example.coffeeOrderService.model.product.QProduct product;

    public final NumberPath<java.math.BigDecimal> totalPrice = createNumber("totalPrice", java.math.BigDecimal.class);

    public final com.example.coffeeOrderService.model.user.QUser user;

    public QPaymentHistory(String variable) {
        this(PaymentHistory.class, forVariable(variable), INITS);
    }

    public QPaymentHistory(Path<? extends PaymentHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentHistory(PathMetadata metadata, PathInits inits) {
        this(PaymentHistory.class, metadata, inits);
    }

    public QPaymentHistory(Class<? extends PaymentHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new com.example.coffeeOrderService.model.order.QOrder(forProperty("order"), inits.get("order")) : null;
        this.product = inits.isInitialized("product") ? new com.example.coffeeOrderService.model.product.QProduct(forProperty("product"), inits.get("product")) : null;
        this.user = inits.isInitialized("user") ? new com.example.coffeeOrderService.model.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

