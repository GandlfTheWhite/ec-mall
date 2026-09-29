package com.zyd.ecmall.controller;

import com.zyd.ecmall.service.ProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/product-images")
public class ProductImageController {
    private final ProductImageService images;
    public ProductImageController(ProductImageService images) { this.images = images; }

    // /api/admin/** の既存インターセプターで管理者を確認する。
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageService.UploadResult upload(@RequestPart("file") MultipartFile file) {
        return images.upload(file);
    }
}
