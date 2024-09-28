package com.example.coffeeOrderService.service.order;

import com.example.coffeeOrderService.common.util.JwtForOrderUtil;
import com.example.coffeeOrderService.dto.OrderDto;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.order.Order;
import com.example.coffeeOrderService.model.order.OrderRepository;
import com.example.coffeeOrderService.model.order.OrderStatus;
import com.example.coffeeOrderService.model.order.PayMethod;
import com.example.coffeeOrderService.model.orderItem.OrderItem;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.model.user.User;

import com.example.coffeeOrderService.request.payment.RequestOrder;
import com.example.coffeeOrderService.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final CartService cartService;


    @Transactional
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        Order order = createOrder(cart);  // 주문 생성
        List<OrderItem> orderItems = createOrderItems(order, cart); // 장바구니 항목을 주문 항목으로 변환
        order.setOrderItems(new HashSet<>(orderItems));  // 주문 항목 설정
        order.setTotalAmount(calculateToTalAmount(orderItems));  // 총 금액 계산
        Order orderSaved = orderRepository.save(order); // 주문 저장

        cartService.clearCart(cart.getId()); // 장바구니 비우기

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
                .orderStatus(OrderStatus.PENDING)  // 임시 주문 상태
                .orderDate(LocalDate.now())
                .build();
    }

    // Cart 객체에서 **장바구니 항목(CartItem)**을 가져와, 이를 **주문 항목(OrderItem)**으로 변환
    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getCartItems().stream()   // 일반 스트림 사용
                .map(cartItem -> {
                    // 각 CartItem에서 상품(Product) 정보를 가져옴
                    Product product = cartItem.getProduct();
                    // 재고가 충분하지 않을 경우 예외 발생
//                    if (product.getInventory() < cartItem.getQuantity()) {
//                        throw new IllegalStateException("재고가 부족합니다.");
//                    }
                    // 현재 상품의 재고를 가져와서, 사용자가 구매한 수량만큼 재고에서 차감하고, 그 결과를 데이터베이스에 저장
                    product.setInventory(product.getInventory() - cartItem.getQuantity()); // ***
                    productRepository.save(product);
                    return new OrderItem(
                            order,
                            product,
                            cartItem.getQuantity(),
                            cartItem.getUnitPrice()
                    );
                }).toList();
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

}
