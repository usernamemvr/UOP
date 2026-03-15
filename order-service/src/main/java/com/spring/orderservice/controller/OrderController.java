package com.spring.orderservice.controller;

import com.spring.orderservice.dto.OrderRequestDto;
import com.spring.orderservice.dto.OrderResponseDto;
import com.spring.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders();
    }
}

