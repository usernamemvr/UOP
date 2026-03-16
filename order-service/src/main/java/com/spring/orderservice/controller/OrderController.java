package com.spring.orderservice.controller;

import com.spring.orderservice.dto.OrderRequestDto;
import com.spring.orderservice.dto.OrderResponseDto;
import com.spring.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @PostMapping
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto request) {
        log.info("Received order creation request for userId={} amount={} currency={}",
                request.getUserId(),
                request.getAmount(),
                request.getCurrency());

        OrderResponseDto response = orderService.createOrder(request);

        log.info("Order successfully created with orderId={} userId={} paymentId={} status={}",
                response.getOrderId(),
                response.getUserId(),
                response.getPaymentId(),
                response.getStatus());

        return response;
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrder(@PathVariable Long id) {
        log.info("Received request to fetch order with id={}", id);

        OrderResponseDto response = orderService.getOrderById(id);

        log.info("Returning order details for id={} userId={} paymentId={} status={}",
                response.getOrderId(),
                response.getUserId(),
                response.getPaymentId(),
                response.getStatus());

        return response;
    }

    @GetMapping
    public List<OrderResponseDto> getAllOrders() {
        log.info("Received request to fetch all orders");
        List<OrderResponseDto> orders = orderService.getAllOrders();
        log.info("Returning {} orders", orders.size());
        return orders;
    }
}

