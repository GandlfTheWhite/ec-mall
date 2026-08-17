package com.zyd.ecmall.dto;

import java.util.List;

/**
 * ページング結果汎用クラス / 分页结果通用类
 */
public class PageResult<T> {

    private List<T> content;      // データリスト / 数据列表
    private int totalElements;    // 総件数 / 总记录数
    private int totalPages;       // 総ページ数 / 总页数
    private int currentPage;      // 現在のページ / 当前页码
    private int pageSize;         // 1ページの件数 / 每页条数

    public PageResult(List<T> content, int totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }

    // Getter / Setter
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}