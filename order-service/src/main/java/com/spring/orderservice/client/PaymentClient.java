package com.spring.orderservice.client;

import com.spring.orderservice.dto.PaymentRequestDto;
import com.spring.orderservice.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestTemplate restTemplate;

    private static final String PAYMENT_SERVICE_BASE_URL = "http://payment-service/payments";
    // can be injected as ENV's from IntelliJ IDE

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        HttpEntity<PaymentRequestDto> entity = new HttpEntity<>(request);
        ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
                PAYMENT_SERVICE_BASE_URL,
                HttpMethod.POST,
                entity,
                PaymentResponseDto.class
        );
        return response.getBody();
    }
}

