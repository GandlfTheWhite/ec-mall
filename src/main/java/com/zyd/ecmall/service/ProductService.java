package com.zyd.ecmall.service;

import com.zyd.ecmall.dto.PageResult;
import com.zyd.ecmall.dto.ProductCreateRequest;
import com.zyd.ecmall.dto.ProductSearchRequest;
import com.zyd.ecmall.dto.ProductUpdateRequest;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.exception.MemberNotFoundException; // 可以新建 ProductNotFoundException，但先用通用的
import com.zyd.ecmall.exception.ProductNotFoundException;
import com.zyd.ecmall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    // 查询所有商品
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    // 根据ID查询商品
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    // 新增商品
    public Product createProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(1); // 默认上架

        productMapper.insert(product);
        return productMapper.selectById(product.getId());
    }

    // 更新商品
    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在，ID：" + id);
        }

        // 只更新传入的非空字段
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        productMapper.update(product);
        return productMapper.selectById(id);
    }

    // 删除商品
    public boolean deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在，ID：" + id);
        }
        return productMapper.deleteById(id) > 0;
    }


    /**
     * 商品を条件検索＋ページングで取得する / 条件搜索+分页获取商品
     */
    public PageResult<Product> searchProducts(ProductSearchRequest request) {
        // 1. ページ番号とサイズを補正（安全対策）
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getSize() == null || request.getSize() < 1) {
            request.setSize(10);
        }
        // 最大サイズ制限（悪意のある大量リクエストを防ぐ）
        if (request.getSize() > 100) {
            request.setSize(100);
        }

        // 2. 総件数を取得（SQL実行1回目）
        long totalElements = productMapper.countBySearch(request);

        // 3. 总件数が0なら空リストを即返却（2回目のSQLを回避してパフォーマンス向上）
        if (totalElements == 0) {
            return new PageResult<>(List.of(), 0, request.getPage(), request.getSize());
        }

        // 4. offsetを計算
        int offset = (request.getPage() - 1) * request.getSize();

        // 5. データリストを取得（SQL実行2回目）
        List<Product> content = productMapper.searchProducts(request, offset);

        // 6. 結果をラップして返却
        return new PageResult<>(content, (int) totalElements, request.getPage(), request.getSize());
    }
}