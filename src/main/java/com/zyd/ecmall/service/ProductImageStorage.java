package com.zyd.ecmall.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class ProductImageStorage {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final S3Client s3;
    private final String bucket;
    private final String publicBaseUrl;

    public ProductImageStorage(S3Client s3, String bucket, String publicBaseUrl) {
        if (bucket == null || bucket.isBlank()) throw new IllegalArgumentException("IMAGE_BUCKET is required");
        if (publicBaseUrl == null) throw new IllegalArgumentException("IMAGE_PUBLIC_BASE_URL is required");
        URI uri = URI.create(publicBaseUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("IMAGE_PUBLIC_BASE_URL must be an HTTPS origin or prefix");
        }
        this.s3 = s3;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        if (this.publicBaseUrl.length() + 50 > 255) {
            throw new IllegalArgumentException("IMAGE_PUBLIC_BASE_URL is too long for products.image_url");
        }
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw badRequest("画像を選択してください。");
        if (file.getSize() > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                "画像は5MB以下にしてください。");
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "画像は5MB以下にしてください。");
            ImageType type = ImageType.detect(bytes);
            if (type == null || !type.contentType.equalsIgnoreCase(file.getContentType())) {
                throw badRequest("JPEG、PNG、WebP形式の画像を選択してください。");
            }
            String key = "products/" + UUID.randomUUID() + "." + type.extension;
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                    .contentType(type.contentType).build(), RequestBody.fromBytes(bytes));
            return publicBaseUrl + "/" + key;
        } catch (IOException | SdkException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "画像の保存に失敗しました。", e);
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private enum ImageType {
        JPEG("image/jpeg", "jpg"), PNG("image/png", "png"), WEBP("image/webp", "webp");
        final String contentType;
        final String extension;
        ImageType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        static ImageType detect(byte[] b) {
            if (b.length >= 3 && (b[0] & 255) == 0xff && (b[1] & 255) == 0xd8 && (b[2] & 255) == 0xff) return JPEG;
            if (b.length >= 8 && (b[0] & 255) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && b[4] == 13 && b[5] == 10 && (b[6] & 255) == 0x1a && b[7] == 10) return PNG;
            if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return WEBP;
            return null;
        }
    }
}
