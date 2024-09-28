package com.example.coffeeOrderService.service.payment;

import com.example.coffeeOrderService.common.auth.service.AuthService;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.order.Order;
import com.example.coffeeOrderService.model.order.OrderRepository;
import com.example.coffeeOrderService.model.order.PayMethod;
import com.example.coffeeOrderService.model.orderItem.OrderItem;
import com.example.coffeeOrderService.model.payment.PaymentHistory;
import com.example.coffeeOrderService.model.payment.PaymentHistoryRepository;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import com.example.coffeeOrderService.request.payment.RequestPayment;
import com.example.coffeeOrderService.service.order.OrderService;
import com.example.coffeeOrderService.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentHistoryRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final AuthService authService;
    private OrderService orderService;


    public void processPaymentDone(RequestPayment request, String imp_uid) {

        Long orderId = request.getOrderId();

        User user = userService.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        Optional<User> currentUser = authService.getAuthenticatedUser();
//        if (!user.equals(currentUser)) {
//            throw new IllegalStateException("로그인 사용자와 주문자가 서로 다릅니다.");
//        }

        // 결제된 금액 확인
        BigDecimal totalPrice = request.getPaymentPrice();
//        List<Long> productMgtIdList = request.getInventoryIdList();

        //orders 테이블에서 해당 부분 결제 true 처리
        Order currentOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문 정보를 찾을 수 없습니다."));
        currentOrder.setPaymentStatus(true);  // 결제 완료 상태로 설정

        // PaymentHistory 테이블에 저장할 Orders 객체
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 주문서를 찾을 수 없습니다. Id : " + orderId));

        // imp_uid, pay_method 및 결제 상태를 업데이트
        order.setImpUid(request.getImpUid());
//        order.setPayMethod(PayMethod.valueOf(request.getPayMethod()));
        order.setPaymentStatus(true);  // 결제 상태를 true로 변경

        // 주문한 상품들에 대해 각각 결제내역 저장
        createPaymentHistory(order, user, totalPrice, imp_uid);
    }


    // 결제내역 테이블 저장하는 메서드
    private void createPaymentHistory(Order order, User user, BigDecimal totalPrice, String paymentUid) {
        // 주문에 포함된 상품들을 각각 결제 내역으로 저장
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();

            // PaymentHistory 객체 생성
            PaymentHistory paymentHistory = new PaymentHistory(
                    user,
                    order,
                    product,
                    paymentUid,
                    totalPrice, // 총 결제 금액
                    LocalDate.now(), // 결제 일자
                    true // 결제 상태 (완료)
            );

            // 결제 내역을 저장
            paymentRepository.save(paymentHistory);
        }

    }


}

