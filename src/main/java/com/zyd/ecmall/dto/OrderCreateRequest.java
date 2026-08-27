package com.zyd.ecmall.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 注文作成リクエスト / 创建订单请求
 */
public class OrderCreateRequest {

    @NotBlank(message = "配送先住所は必須です")
    private String shippingAddress;

    @NotBlank(message = "受取人名は必須です")
    private String receiverName;

    @NotBlank(message = "受取人電話番号は必須です")
    private String receiverPhone;

    // Getter/Setter
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
}