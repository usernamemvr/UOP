package com.spring.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    private Long orderId;
    private Long userId;
    private Double amount;
    private String currency;
}

