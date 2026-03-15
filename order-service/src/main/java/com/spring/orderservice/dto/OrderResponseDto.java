package com.spring.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    private Long paymentId;
    private Double amount;
    private String status;
}

