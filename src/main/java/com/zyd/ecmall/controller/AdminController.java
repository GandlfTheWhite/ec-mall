package com.zyd.ecmall.controller;

import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.entity.Order;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.service.MemberService;
import com.zyd.ecmall.service.OrderService;
import com.zyd.ecmall.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final ProductService productService;

    public AdminController(MemberService memberService, OrderService orderService, ProductService productService) {
        this.memberService = memberService;
        this.orderService = orderService;
        this.productService = productService;
    }

    // ---- 会員管理 ----
    @GetMapping("/members")
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @PutMapping("/members/{id}/role")
    public ResponseEntity<Member> updateMemberRole(@PathVariable Long id, @RequestParam String role) {
        // role は "USER" または "ADMIN" のみ許可
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("無効なロールです。USER または ADMIN を指定してください。");
        }
        Member updated = memberService.updateRole(id, role);
        return ResponseEntity.ok(updated);
    }

    // ---- 注文管理（全ユーザー対象） ----
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders(); // 新しく実装するメソッド
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        // status: 0=未払い, 1=支払済み, 2=発送済み, 3=完了, 4=キャンセル
        Order order = orderService.updateStatus(id, status);
        return ResponseEntity.ok(order);
    }

    // ---- 商品管理（追加の管理者機能） ----
    @PutMapping("/products/{id}/status")
    public ResponseEntity<Product> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        // status: 0=非公開, 1=公開
        Product product = productService.updateStatus(id, status);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable Long id, @RequestParam Integer stock) {
        Product product = productService.updateStock(id, stock);
        return ResponseEntity.ok(product);
    }
}
