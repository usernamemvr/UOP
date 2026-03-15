package com.spring.paymentservice.controller;

import com.spring.paymentservice.dto.PaymentRequestDto;
import com.spring.paymentservice.dto.PaymentResponseDto;
import com.spring.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponseDto createPayment(@RequestBody PaymentRequestDto request) {
        return paymentService.processPayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponseDto getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponseDto> getPaymentsByOrder(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
}

