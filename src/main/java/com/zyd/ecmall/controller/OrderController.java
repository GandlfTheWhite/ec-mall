package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.OrderCreateRequest;
import com.zyd.ecmall.entity.Order;
import com.zyd.ecmall.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 支払い処理（モック） / 支付处理（模拟）
     * POST /api/orders/{id}/pay
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long memberId = (Long) request.getAttribute("memberId");
        Order order = orderService.processPayment(id, memberId);
        return ResponseEntity.ok(order);
    }

    /**
     * 自分の注文一覧を取得 / 获取自己的订单列表
     */
    @GetMapping
    public List<Order> getMyOrders(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return orderService.getOrdersByMember(memberId);
    }
    
    /**
     * 注文詳細を取得（明細付き） / 获取订单详情
     */
    @GetMapping("/{id}")
    public Order getOrderDetail(HttpServletRequest request, @PathVariable Long id) {
        Long memberId = (Long) request.getAttribute("memberId");
        return orderService.getOrderDetail(id, memberId);
    }
    
}
