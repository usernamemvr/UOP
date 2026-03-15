package com.spring.orderservice.service;

import com.spring.orderservice.client.PaymentClient;
import com.spring.orderservice.client.UserClient;
import com.spring.orderservice.dto.*;
import com.spring.orderservice.entity.Order;
import com.spring.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final PaymentClient paymentClient;

    public OrderResponseDto createOrder(OrderRequestDto request) {
        // 1. Validate user exists
        UserDto user = userClient.getUserById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found for id: " + request.getUserId());
        }

        // 2. Create temporary order object (without payment info yet)
        Order order = Order.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 3. Call payment-service to process payment
        PaymentRequestDto paymentRequest = new PaymentRequestDto(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency()
        );
        PaymentResponseDto paymentResponse = paymentClient.processPayment(paymentRequest);

        if (paymentResponse == null) {
            throw new RuntimeException("Payment processing failed for order id: " + savedOrder.getId());
        }

        // 4. Update and save order with paymentId and status
        savedOrder.setPaymentId(paymentResponse.getPaymentId());
        savedOrder.setStatus(paymentResponse.getStatus());
        Order finalOrder = orderRepository.save(savedOrder);

        // 5. Return OrderResponseDto
        return new OrderResponseDto(
                finalOrder.getId(),
                finalOrder.getUserId(),
                finalOrder.getPaymentId(),
                finalOrder.getAmount(),
                finalOrder.getStatus()
        );
    }

    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + id));

        return new OrderResponseDto(
                order.getId(),
                order.getUserId(),
                order.getPaymentId(),
                order.getAmount(),
                order.getStatus()
        );
    }

    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(o -> new OrderResponseDto(
                        o.getId(),
                        o.getUserId(),
                        o.getPaymentId(),
                        o.getAmount(),
                        o.getStatus()
                ))
                .toList();
    }
}

