package com.zyd.ecmall.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class ProductUpdateRequest {
    @Pattern(regexp = "(?s).*\\S.*", message = "商品名を入力してください")
    private String name;
    private String description;
    @DecimalMin(value = "0.01", message = "価格は0.01以上で入力してください")
    private BigDecimal price;
    @Min(value = 0, message = "在庫数は0以上で入力してください")
    private Integer stock;
    private String category;
    private String imageUrl;
    @Min(0)
    @Max(1)
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
