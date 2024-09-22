package com.example.coffeeOrderService.service.order;

import com.example.coffeeOrderService.dto.OrderDto;
import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.order.Order;
import com.example.coffeeOrderService.model.order.OrderRepository;
import com.example.coffeeOrderService.model.order.OrderStatus;
import com.example.coffeeOrderService.model.orderItem.OrderItem;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;


@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;


    @Transactional
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        Order order = createOrder(cart);  //
        List<OrderItem> orderItems = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItems));
        order.setTotalAmount(calculateToTalAmount(orderItems));
        Order orderSaved = orderRepository.save(order);

        cartService.clearCart(cart.getId());

        return orderSaved;
    }

    private BigDecimal calculateToTalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrder(Cart cart) {
        return Order.builder()
                .user(cart.getUser())
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .build();
    }

    // Cart 객체에서 **장바구니 항목(CartItem)**을 가져와, 이를 **주문 항목(OrderItem)**으로 변환
    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getCartItems().stream()
                .map(cartItem -> {
                    // 각 CartItem에서 상품(Product) 정보를 가져옴
                    Product product = cartItem.getProduct();
                    // 현재 상품의 재고를 가져와서, 사용자가 구매한 수량만큼 재고에서 차감하고, 그 결과를 데이터베이스에 저장
                    product.setInventory(product.getInventory() - cartItem.getQuantity());
                    productRepository.save(product);
                    return new OrderItem(
                            order,
                            product,
                            cartItem.getQuantity(),
                            cartItem.getUnitPrice()
                    );
                }).toList();
    }


    public OrderDto getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found!"));
    }

    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }


    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

}
