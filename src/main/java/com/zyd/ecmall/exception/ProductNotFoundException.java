package com.zyd.ecmall.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("商品が見つかりません。ID：" + id);  // 日本語コメント
    }
}