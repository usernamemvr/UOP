package com.spring.orderservice.service;

import com.spring.orderservice.client.PaymentClient;
import com.spring.orderservice.client.UserClient;
import com.spring.orderservice.dto.*;
import com.spring.orderservice.entity.Order;
import com.spring.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        kafkaTemplate.send("order-events", request);
        log.info("Event produced to order-event topic. {}",request);

        // 5. Return OrderResponseDto
        return new OrderResponseDto(
                savedOrder.getId(),
                savedOrder.getUserId(),
                null,
                savedOrder.getAmount(),
                savedOrder.getStatus()
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

