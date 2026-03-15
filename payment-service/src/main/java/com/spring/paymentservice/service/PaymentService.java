package com.spring.paymentservice.service;

import com.spring.paymentservice.dto.PaymentRequestDto;
import com.spring.paymentservice.dto.PaymentResponseDto;
import com.spring.paymentservice.entity.Payment;
import com.spring.paymentservice.enums.PaymentStatus;
import com.spring.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.SUCCESS) // simulate success
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponseDto(
                saved.getId(),
                saved.getStatus(),
                saved.getAmount()
        );
    }

    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found for id: " + id));

        return new PaymentResponseDto(
                payment.getId(),
                payment.getStatus(),
                payment.getAmount()
        );
    }

    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(p -> new PaymentResponseDto(
                        p.getId(),
                        p.getStatus(),
                        p.getAmount()
                ))
                .toList();
    }
}

