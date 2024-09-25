package com.example.coffeeOrderService.model.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1321114303L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final StringPath address = createString("address");

    public final com.example.coffeeOrderService.model.cart.QCart cart;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isOAuth2 = createBoolean("isOAuth2");

    public final StringPath nickname = createString("nickname");

    public final ListPath<com.example.coffeeOrderService.model.order.Order, com.example.coffeeOrderService.model.order.QOrder> orders = this.<com.example.coffeeOrderService.model.order.Order, com.example.coffeeOrderService.model.order.QOrder>createList("orders", com.example.coffeeOrderService.model.order.Order.class, com.example.coffeeOrderService.model.order.QOrder.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final CollectionPath<Role, QRole> roles = this.<Role, QRole>createCollection("roles", Role.class, QRole.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cart = inits.isInitialized("cart") ? new com.example.coffeeOrderService.model.cart.QCart(forProperty("cart"), inits.get("cart")) : null;
    }

}

