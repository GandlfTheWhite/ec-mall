package com.zyd.ecmall.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * カート情報レスポンス / 购物车信息响应
 */
public class CartResponse {

    private Long cartId;
    private List<CartItemDetail> items;
    private BigDecimal totalPrice; // 合計金額

    // Getter/Setter
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public List<CartItemDetail> getItems() { return items; }
    public void setItems(List<CartItemDetail> items) { this.items = items; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    /**
     * カート明細（商品情報付き） / 购物车明细（附带商品信息）
     */
    public static class CartItemDetail {
        private Long productId;
        private String productName;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal priceAtAdd; // カート追加時の価格
        private BigDecimal currentPrice; // 現在の最新価格（参考値）

        // Getter/Setter（省略）
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPriceAtAdd() { return priceAtAdd; }
        public void setPriceAtAdd(BigDecimal priceAtAdd) { this.priceAtAdd = priceAtAdd; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    }
}