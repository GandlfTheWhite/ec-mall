package com.zyd.ecmall.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductCreateRequest {

    @NotBlank(message = "商品名は必須です")
    private String name;

    private String description;

    @NotNull(message = "価格は必須です")
    @DecimalMin(value = "0.01", message = "価格は0より大きい値を入力してください")
    private BigDecimal price;

    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上で入力してください")
    private Integer stock;

    private String category;
    private String imageUrl;

    // Getter/Setter...
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
}