package com.example.coffeeOrderService.service.cart;

import com.example.coffeeOrderService.dto.CartDto;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.cart.CartRepository;
import com.example.coffeeOrderService.model.cartItem.CartItemRepository;
import com.example.coffeeOrderService.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public Cart getCart(Long id) {
        Cart cart = cartRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found!")
        );
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return cartRepository.save(cart);
    }


    @Transactional
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getCartItems().clear();
        cartRepository.deleteById(id);
    }


    public BigDecimal getTotalPrice(Long id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }


    @Transactional
    public Cart initializeNewCart(User user) {
        return Optional.ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }


    // N+1 문제 해결 : JOIN FETCH -> 한 번의 쿼리로 가져올 수 있음
    public Cart getCartByUserId(Long userId) {
//        log.info("Fetching cart for userId: {}", userId);
//        return cartRepository.findByUserIdWithItemsAndProducts(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));
        return cartRepository.findByUserId(userId);
    }


//    public CartDto convertToDto(Cart cart) {
////        Hibernate.initialize(cart.getCartItems());  // Lazy Loading된 CartItems 강제 로드
//        return modelMapper.map(cart, CartDto.class);
//    }

    public CartDto convertToDto(Cart cart) {
        return CartDto.fromCart(cart);
    }

}
