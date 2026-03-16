package com.spring.orderservice.client;

import com.spring.orderservice.dto.PaymentRequestDto;
import com.spring.orderservice.dto.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments")
    PaymentResponseDto processPayment(@RequestBody PaymentRequestDto request);
}

