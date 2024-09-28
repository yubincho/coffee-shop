package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.dto.OrderDto;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.order.Order;

import com.example.coffeeOrderService.request.payment.RequestOrder;
import com.example.coffeeOrderService.response.ApiResponse;
import com.example.coffeeOrderService.service.cart.CartService;
import com.example.coffeeOrderService.service.order.OrderService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final HttpSession httpSession;


    @PostMapping("/user/place-order")
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
                                         // @RequestParam Long userId
//        Long userId = 1L;  //
        try {
            Order temporaryOrder  = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(temporaryOrder);

            log.debug("Order placed successfully for user: {}", userId);
            return ResponseEntity.ok().body(new ApiResponse("Order Success!", orderDto));
        } catch (Exception e) {
            log.error("Error while placing order for user: {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error Occured!", e.getMessage()));
        }
    }


    /**
     * 주문서에서 입력받아 최종 주문 테이블 생성
     * @param
     * @return
     */
    @PostMapping("/done")
    public ResponseEntity<Object> completeOrder(@RequestBody RequestOrder requestOrder) {
        Order completedOrder = orderService.completeOrder(requestOrder);

        // 주문 완료 후 DTO로 변환하여 응답
        OrderDto orderDto = orderService.convertToDto(completedOrder);

        return ResponseEntity.ok(orderDto);
    }



    /** ************************************************************************************* */

    @GetMapping("/{orderId}/order")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable Long orderId) {
        try {
            OrderDto orderDto = orderService.getOrder(orderId);
            return ResponseEntity.ok().body(new ApiResponse("Success!", orderDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("No Order found!", e.getMessage()));
        }
    }


    @GetMapping("/user/{userId}/order")
    public ResponseEntity<ApiResponse> getOrderByUserId(@PathVariable Long userId) {
        try {
            List<OrderDto> orders = orderService.getUserOrders(userId);
            log.info(orders.toString());
            return ResponseEntity.ok().body(new ApiResponse("Success!", orders));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Oops! No Order found!", e.getMessage()));
        }
    }


}
