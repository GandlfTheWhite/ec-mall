package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.CartItemRequest;
import com.zyd.ecmall.dto.CartResponse;
import com.zyd.ecmall.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * カート詳細取得（GET /api/cart）
     */
    @GetMapping
    public CartResponse getCart(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return cartService.getCart(memberId);
    }

    /**
     * カートに商品を追加（POST /api/cart/items）
     */
    @PostMapping("/items")
    public ResponseEntity<Void> addItem(HttpServletRequest request, @Valid @RequestBody CartItemRequest req) {
        Long memberId = (Long) request.getAttribute("memberId");
        cartService.addItemToCart(memberId, req);
        return ResponseEntity.ok().build();
    }

    /**
     * カート内商品数量を更新（PUT /api/cart/items/{productId}）
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<Void> updateQuantity(HttpServletRequest request,
                                               @PathVariable Long productId,
                                               @RequestParam Integer quantity) {
        Long memberId = (Long) request.getAttribute("memberId");
        cartService.updateItemQuantity(memberId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * カートから商品を削除（DELETE /api/cart/items/{productId}）
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(HttpServletRequest request, @PathVariable Long productId) {
        Long memberId = (Long) request.getAttribute("memberId");
        cartService.removeItemFromCart(memberId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * カートを空にする（DELETE /api/cart）
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        cartService.clearCart(memberId);
        return ResponseEntity.noContent().build();
    }
}