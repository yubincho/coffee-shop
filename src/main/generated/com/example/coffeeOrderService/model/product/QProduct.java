package com.example.coffeeOrderService.model.product;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1367394143L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProduct product = new QProduct("product");

    public final StringPath brand = createString("brand");

    public final com.example.coffeeOrderService.model.category.QCategory category;

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.example.coffeeOrderService.model.image.Image, com.example.coffeeOrderService.model.image.QImage> images = this.<com.example.coffeeOrderService.model.image.Image, com.example.coffeeOrderService.model.image.QImage>createList("images", com.example.coffeeOrderService.model.image.Image.class, com.example.coffeeOrderService.model.image.QImage.class, PathInits.DIRECT2);

    public final NumberPath<Integer> inventory = createNumber("inventory", Integer.class);

    public final StringPath name = createString("name");

    public final ListPath<com.example.coffeeOrderService.model.orderItem.OrderItem, com.example.coffeeOrderService.model.orderItem.QOrderItem> orderItem = this.<com.example.coffeeOrderService.model.orderItem.OrderItem, com.example.coffeeOrderService.model.orderItem.QOrderItem>createList("orderItem", com.example.coffeeOrderService.model.orderItem.OrderItem.class, com.example.coffeeOrderService.model.orderItem.QOrderItem.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final EnumPath<ProductStatus> status = createEnum("status", ProductStatus.class);

    public QProduct(String variable) {
        this(Product.class, forVariable(variable), INITS);
    }

    public QProduct(Path<? extends Product> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProduct(PathMetadata metadata, PathInits inits) {
        this(Product.class, metadata, inits);
    }

    public QProduct(Class<? extends Product> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.example.coffeeOrderService.model.category.QCategory(forProperty("category")) : null;
    }

}

