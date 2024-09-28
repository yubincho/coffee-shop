package com.example.coffeeOrderService.model.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = 365870815L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final StringPath impUid = createString("impUid");

    public final StringPath merchantUid = createString("merchantUid");

    public final DatePath<java.time.LocalDate> orderDate = createDate("orderDate", java.time.LocalDate.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final SetPath<com.example.coffeeOrderService.model.orderItem.OrderItem, com.example.coffeeOrderService.model.orderItem.QOrderItem> orderItems = this.<com.example.coffeeOrderService.model.orderItem.OrderItem, com.example.coffeeOrderService.model.orderItem.QOrderItem>createSet("orderItems", com.example.coffeeOrderService.model.orderItem.OrderItem.class, com.example.coffeeOrderService.model.orderItem.QOrderItem.class, PathInits.DIRECT2);

    public final EnumPath<OrderStatus> orderStatus = createEnum("orderStatus", OrderStatus.class);

    public final ListPath<com.example.coffeeOrderService.model.payment.PaymentHistory, com.example.coffeeOrderService.model.payment.QPaymentHistory> paymentHistories = this.<com.example.coffeeOrderService.model.payment.PaymentHistory, com.example.coffeeOrderService.model.payment.QPaymentHistory>createList("paymentHistories", com.example.coffeeOrderService.model.payment.PaymentHistory.class, com.example.coffeeOrderService.model.payment.QPaymentHistory.class, PathInits.DIRECT2);

    public final BooleanPath paymentStatus = createBoolean("paymentStatus");

    public final EnumPath<PayMethod> payMethod = createEnum("payMethod", PayMethod.class);

    public final NumberPath<java.math.BigDecimal> totalAmount = createNumber("totalAmount", java.math.BigDecimal.class);

    public final com.example.coffeeOrderService.model.user.QUser user;

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.coffeeOrderService.model.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

