package com.zyd.ecmall.service;

import com.zyd.ecmall.config.ProductImageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ProductImageStorage {
    private static final Logger log = LoggerFactory.getLogger(ProductImageStorage.class);
    private final ProductImageProperties properties;
    private final ObjectProvider<S3Client> clients;

    public ProductImageStorage(ProductImageProperties properties, ObjectProvider<S3Client> clients) {
        this.properties = properties;
        this.clients = clients;
    }

    public String store(String key, String contentType, byte[] bytes) {
        if (!properties.enabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "画像アップロードは未設定です。管理者にお問い合わせください。");
        }
        String base = properties.checkedBaseUrl();
        try {
            // ACL を設定せず、非公開バケットへ保存する。読み取りは CloudFront OAC に限定する。
            clients.getObject().putObject(PutObjectRequest.builder()
                    .bucket(properties.bucket()).key(key).contentType(contentType)
                    .contentLength((long) bytes.length)
                    .cacheControl("public, max-age=31536000, immutable")
                    .contentDisposition("inline").build(), RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            log.warn("商品画像をS3に保存できませんでした: {}", e.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "画像の保存に失敗しました。時間をおいて再度お試しください。");
        }
        return base + "/" + key;
    }
}
