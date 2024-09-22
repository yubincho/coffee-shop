package com.example.coffeeOrderService.service.cart;

import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.cart.CartRepository;
import com.example.coffeeOrderService.model.cartItem.CartItem;
import com.example.coffeeOrderService.model.cartItem.CartItemRepository;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@RequiredArgsConstructor
@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final ProductService productService;


    @Transactional
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);
        // 주어진 productId와 동일한 상품을 찾고, 해당 상품이 장바구니에 없으면 새로운 CartItem을 반환
        CartItem cartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
        if (cartItem == null) {
            // 새로운 CartItem 생성
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        }
        else {
            // 기존 CartItem이 존재할 때 수량 증가
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        // unitPrice가 설정된 후에 총 가격 계산
        cartItem.setTotalPrice();
        cart.addCartItem(cartItem);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }


    @Transactional
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();
                });
        BigDecimal totalAmount = cart.getCartItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }


    @Transactional
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        CartItem itemToRemove = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
        cart.removeCartItem(itemToRemove);
        cartRepository.save(cart);
    }


    public CartItem getCartItem(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Item not found!"));
    }

}
