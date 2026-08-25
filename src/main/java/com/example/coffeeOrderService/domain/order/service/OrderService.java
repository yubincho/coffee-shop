package com.example.coffeeOrderService.domain.order.service;

import com.example.coffeeOrderService.domain.cartItem.repository.CartItemRepository;
import com.example.coffeeOrderService.domain.order.dto.OrderDto;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.domain.cart.entity.Cart;
import com.example.coffeeOrderService.domain.order.entity.Order;
import com.example.coffeeOrderService.domain.order.repository.OrderRepository;
import com.example.coffeeOrderService.domain.order.enums.OrderStatus;
import com.example.coffeeOrderService.domain.order.enums.PayMethod;
import com.example.coffeeOrderService.domain.orderItem.entity.OrderItem;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;

import com.example.coffeeOrderService.domain.payment.dto.RequestOrder;
import com.example.coffeeOrderService.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;


    @Transactional
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        List<OrderItem> orderItems = createOrderItemsForRedisson(cart, true);  // 비관적 락 유지
        // 또는 List<OrderItem> orderItems = createOrderItems(cart);   // 재고 차감 + 항목 생성
        Order order = Order.createOrder(cart.getUser(), orderItems); // 연결 + 총액까지 엔티티가 담당
        Order orderSaved = orderRepository.save(order);

        cartService.clearCart(cart.getId());  // 장바구니 삭제
        return orderSaved;
    }

    @Transactional
    public Order placeOrderForRedisson(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        List<OrderItem> orderItems = createOrderItemsForRedisson(cart, false);  // 일반 조회 (비관적 락 X)
        Order order = Order.createOrder(cart.getUser(), orderItems);

        Order orderSaved = orderRepository.save(order);
        cartService.clearCart(cart.getId());
        return orderSaved;
    }

    // Redisson 락 경로 (Facade에서 호출 — 비관적 락 없이 일반 조회)
    private List<OrderItem> createOrderItemsForRedisson(Cart cart, boolean usePessimisticLock) {
        return cart.getCartItems().stream()
                .map(cartItem -> {
                    Long productId = cartItem.getProduct().getId();

                    Product product = usePessimisticLock ? productRepository.findByIdWithPessimisticLock(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"))
                            : productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                    product.removeStock(cartItem.getQuantity());

                    return OrderItem.builder()
                            .product(product)
                            .quantity(cartItem.getQuantity())
                            .price(cartItem.getUnitPrice())
                            .build();
                })
                .toList();
    }

    // order 인자 제거: order 연결은 팩토리가 담당
    private List<OrderItem> createOrderItems(Cart cart) {
        return cart.getCartItems().stream()
                .map(cartItem -> {
                    Product product = productRepository.findByIdWithPessimisticLock(
                                    cartItem.getProduct().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                    product.removeStock(cartItem.getQuantity());

                    return OrderItem.builder()   // order는 넣지 않음
                            .product(product)
                            .quantity(cartItem.getQuantity())
                            .price(cartItem.getUnitPrice())
                            .build();
                })
                .toList();
    }


    /**
     * 주문 최종 확정 및 저장
     *
     * @return 주문 테이블 저장
     */
//    @Transactional
//    public Order orderConfirm(Order temporaryOrder, RequestOrder request) {
//
//        String merchantUid = generateMerchantUid();  //주문번호 생성
//
//        User user = temporaryOrder.getUser();  // 기존 사용자 객체 가져오기
//        user.setId(temporaryOrder.getUser().getId());
//        user.setNickname(request.getUsername());
//        user.setAddress(request.getAddress());
//
//        // 세션 주문서와 사용자에게 입력받은 정보 합치기
////        temporaryOrder.orderConfirm(request);
//
//        // 주문 정보 갱신
//        temporaryOrder.setUser(user);
//        temporaryOrder.setMerchantUid(merchantUid);
//        temporaryOrder.setTotalAmount(request.getTotalAmount());
//        temporaryOrder.setPayMethod(temporaryOrder.getPayMethod());
//        temporaryOrder.setOrderStatus(OrderStatus.CONFIRMED);  // 최종 주문 상태로 변경
//
//        return orderRepository.save(temporaryOrder);
//    }

    @Transactional
    public Order completeOrder(RequestOrder requestOrder) {
        // 주문을 데이터베이스에서 찾음
        Order order = orderRepository.findById(requestOrder.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // 주문 상태를 CONFIRMED로 변경
        order.confirmOrder();

        // 사용자 정보 업데이트 (null 체크)
        if (requestOrder.getAddress() != null) {
            order.getUser().setAddress(requestOrder.getAddress());
        }
        if (requestOrder.getUsername() != null) {
            order.getUser().setNickname(requestOrder.getUsername());
        }

        // 결제 수단 설정 (null 체크 및 대소문자 처리)
        if (requestOrder.getPayMethod() != null) {
            order.setPayMethod(PayMethod.valueOf(requestOrder.getPayMethod().toUpperCase()));
        }

        // 주문번호 생성 및 설정
        order.setMerchantUid(generateMerchantUid());

        // 변경된 주문 정보 저장
        return orderRepository.save(order);
    }


    // 주문번호 생성 메서드
    private String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDay = today.format(formatter).replace("-", "");

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay +'-'+ uniqueString;
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
        OrderDto orderDto = OrderDto.toDto(order);
        return orderDto;
//        return modelMapper.map(order, OrderDto.class);
    }


    // 주문에 포함될 상품 ID들을 조회 (락 획득용, 데드락 방지 위해 정렬)
    // 중복 제거 + 오름차순 정렬해서 반환
    @Transactional(readOnly = true)
    public List<Long> findProductIdsForOrder(Long userId) {
        List<Long> productIds = cartItemRepository.findProductIdsByUserId(userId);

        if (productIds.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        return productIds.stream()
                .distinct()   // 같은 상품 중복 제거 (락 두 번 잡지 않도록)
                .sorted()     // 오름차순 정렬 → 데드락 방지
                .toList();
    }

}
