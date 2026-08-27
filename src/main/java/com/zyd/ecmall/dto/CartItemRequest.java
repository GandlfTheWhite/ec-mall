package com.zyd.ecmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * カート追加・更新リクエスト / 购物车添加/更新请求
 */
public class CartItemRequest {

    @NotNull(message = "商品IDは必須です")
    private Long productId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上を指定してください")
    private Integer quantity;

    // Getter/Setter
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}