package com.zyd.ecmall.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * カート明細エンティティ / 购物车明细实体
 */
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal priceAtAdd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getter / Setter（全フィールド分生成）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPriceAtAdd() { return priceAtAdd; }
    public void setPriceAtAdd(BigDecimal priceAtAdd) { this.priceAtAdd = priceAtAdd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}