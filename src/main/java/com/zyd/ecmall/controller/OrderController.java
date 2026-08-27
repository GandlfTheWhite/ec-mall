package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.OrderCreateRequest;
import com.zyd.ecmall.entity.Order;
import com.zyd.ecmall.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody OrderCreateRequest req) {
        Long memberId = (Long) request.getAttribute("memberId");
        Order order = orderService.createOrderFromCart(memberId, req);
        return ResponseEntity.ok(order);
    }
}