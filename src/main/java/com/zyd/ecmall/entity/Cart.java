package com.zyd.ecmall.entity;

import java.time.LocalDateTime;

/**
 * カート主エンティティ / 购物车主实体
 */
public class Cart {
    private Long id;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getter / Setter（省略しますが、実際は全フィールドに生成してください）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}