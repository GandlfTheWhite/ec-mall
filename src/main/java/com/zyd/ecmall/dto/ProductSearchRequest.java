package com.zyd.ecmall.dto;

/**
 * 商品検索リクエスト（ページング対応） / 商品搜索请求（支持分页）
 */
public class ProductSearchRequest {

    private String keyword;       // キーワード（商品名に部分一致） / 关键词（模糊匹配商品名）
    private String category;      // カテゴリ（完全一致） / 分类（精确匹配）
    private Double minPrice;      // 最低価格 / 最低价格
    private Double maxPrice;      // 最高価格 / 最高价格
    private Integer page = 1;     // ページ番号（デフォルト1） / 页码（默认1）
    private Integer size = 10;    // 1ページの件数（デフォルト10） / 每页条数（默认10）

    // Getter / Setter
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}