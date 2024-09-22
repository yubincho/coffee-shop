package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.dto.OrderDto;
import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.order.Order;
import com.example.coffeeOrderService.response.ApiResponse;
import com.example.coffeeOrderService.service.order.OrderService;

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


    @PostMapping("/user/place-order")
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok().body(new ApiResponse("Order Success!", orderDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error Occured!", e.getMessage()));
        }
    }


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
