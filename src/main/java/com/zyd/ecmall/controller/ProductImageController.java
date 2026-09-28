package com.zyd.ecmall.controller;

import com.zyd.ecmall.service.ProductImageStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/product-images")
public class ProductImageController {
    private final ObjectProvider<ProductImageStorage> storage;

    public ProductImageController(ObjectProvider<ProductImageStorage> storage) {
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        ProductImageStorage service = storage.getIfAvailable();
        if (service == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "画像アップロードは設定されていません。");
        return Map.of("imageUrl", service.upload(file));
    }
}
