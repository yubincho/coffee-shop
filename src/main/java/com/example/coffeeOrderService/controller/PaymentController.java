package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.order.Order;
import com.example.coffeeOrderService.model.order.OrderRepository;
import com.example.coffeeOrderService.request.payment.RequestPayment;
import com.example.coffeeOrderService.service.payment.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.siot.IamportRestClient.IamportClient;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private IamportClient iamportClient;


    @Value("${import.apiKey}")
    private String apiKey;

    @Value("${import.secretKey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }


    @PostMapping("/order/payment/{imp_uid}")
    public IamportResponse<Payment> validateIamport(@PathVariable String imp_uid, @RequestBody RequestPayment request) throws IamportResponseException, IOException, IamportResponseException, IOException {

        IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);

        log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());

        paymentService.processPaymentDone(request, imp_uid);

        return payment;
    }


}
