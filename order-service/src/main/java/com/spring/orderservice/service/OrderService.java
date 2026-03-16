package com.spring.orderservice.service;

import com.spring.orderservice.client.PaymentClient;
import com.spring.orderservice.client.UserClient;
import com.spring.orderservice.dto.*;
import com.spring.orderservice.entity.Order;
import com.spring.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final PaymentClient paymentClient;

    public OrderResponseDto createOrder(OrderRequestDto request) {
        log.info("Starting order creation workflow for userId={} amount={} currency={}",
                request.getUserId(),
                request.getAmount(),
                request.getCurrency());

        // 1. Validate user exists
        log.info("Calling user-service to fetch user with id={}", request.getUserId());
        UserDto user = userClient.getUserById(request.getUserId());
        if (user == null) {
            log.error("User-service returned null for id={}", request.getUserId());
            throw new RuntimeException("User not found for id: " + request.getUserId());
        }
        log.info("User-service returned user id={} name={}", user.getId(), user.getName());

        // 2. Create temporary order object (without payment info yet)
        log.info("Creating temporary order entity with status=PENDING for userId={}", request.getUserId());
        Order order = Order.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 3. Call payment-service to process payment
        log.info("Calling payment-service to process payment for orderId={} userId={} amount={} currency={}",
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency());
        PaymentRequestDto paymentRequest = new PaymentRequestDto(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency()
        );
        PaymentResponseDto paymentResponse = paymentClient.processPayment(paymentRequest);

        if (paymentResponse == null) {
            log.error("Payment-service returned null for orderId={}", savedOrder.getId());
            throw new RuntimeException("Payment processing failed for order id: " + savedOrder.getId());
        }
        log.info("Payment processed with paymentId={} status={} amount={}",
                paymentResponse.getPaymentId(),
                paymentResponse.getStatus(),
                paymentResponse.getAmount());

        // 4. Update and save order with paymentId and status
        log.info("Updating order with paymentId={} status={} and saving to database",
                paymentResponse.getPaymentId(),
                paymentResponse.getStatus());
        savedOrder.setPaymentId(paymentResponse.getPaymentId());
        savedOrder.setStatus(paymentResponse.getStatus());
        Order finalOrder = orderRepository.save(savedOrder);

        // 5. Return OrderResponseDto
        log.info("Order creation workflow completed successfully for orderId={} userId={} paymentId={} status={}",
                finalOrder.getId(),
                finalOrder.getUserId(),
                finalOrder.getPaymentId(),
                finalOrder.getStatus());
        return new OrderResponseDto(
                finalOrder.getId(),
                finalOrder.getUserId(),
                finalOrder.getPaymentId(),
                finalOrder.getAmount(),
                finalOrder.getStatus()
        );
    }

    public OrderResponseDto getOrderById(Long id) {
        log.info("Fetching order by id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + id));

        log.info("Order found for id={} userId={} paymentId={} status={}",
                order.getId(),
                order.getUserId(),
                order.getPaymentId(),
                order.getStatus());

        return new OrderResponseDto(
                order.getId(),
                order.getUserId(),
                order.getPaymentId(),
                order.getAmount(),
                order.getStatus()
        );
    }

    public List<OrderResponseDto> getAllOrders() {
        log.info("Fetching all orders from database");
        List<Order> orders = orderRepository.findAll();
        log.info("Fetched {} orders from database", orders.size());

        return orders
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

