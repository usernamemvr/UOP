package com.spring.paymentservice.dto;

import com.spring.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Long paymentId;
    private PaymentStatus status;
    private Double amount;
}

