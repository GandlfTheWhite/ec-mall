package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.PageResult;
import com.zyd.ecmall.dto.ProductCreateRequest;
import com.zyd.ecmall.dto.ProductSearchRequest;
import com.zyd.ecmall.dto.ProductUpdateRequest;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET 公开（不需要 Token）
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // GET 公开（不需要 Token）
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // POST 需要 JWT（新增商品）
    @PostMapping
    public Product createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    // PUT 需要 JWT（更新商品）
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id,
                                 @Valid @RequestBody ProductUpdateRequest request) {
        return productService.updateProduct(id, request);
    }

    // DELETE 需要 JWT（删除商品）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ---- 🆕 商品検索（分页+条件） ----
    /**
     * 商品を検索する（ページング対応、ログイン不要） / 搜索商品（支持分页，无需登录）
     * 例: GET /api/products/search?keyword=iPhone&category=手机&page=1&size=5
     */
    @GetMapping("/search")
    public PageResult<Product> searchProducts(ProductSearchRequest request) {
        // Spring MVC がクエリパラメータを自動で ProductSearchRequest にマッピングしてくれる
        return productService.searchProducts(request);
    }

}