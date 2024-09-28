package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.dto.OrderDto;
import com.example.coffeeOrderService.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor
@Controller
public class HomeController {

    private final OrderService orderService;

    @GetMapping("/api/v1/orders/done")
    public String showOrderDonePage(@RequestParam Long orderId, Model model) {
        OrderDto orderDto = orderService.getOrder(orderId);
        model.addAttribute("orderDto", orderDto);
        return "orderDone"; // Thymeleaf 템플릿 파일 이름
    }

    @GetMapping("/api/v1/payments/order")
    public String showPaymentPage(@RequestParam Long orderId, Model model) {
        OrderDto orderDto = orderService.getOrder(orderId);
        model.addAttribute("orderDto", orderDto);
        return "payment"; // Thymeleaf 템플릿 파일 이름
    }
}
