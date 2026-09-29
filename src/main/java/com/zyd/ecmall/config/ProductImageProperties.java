package com.zyd.ecmall.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.product-images")
public record ProductImageProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("") String bucket,
        @DefaultValue("") String region,
        @DefaultValue("") String publicBaseUrl,
        @DefaultValue("") String frontendBucket) {

    public String checkedBaseUrl() {
        if (bucket == null || !bucket.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]")
                || frontendBucket == null || !frontendBucket.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]")
                || bucket.equals(frontendBucket) || region == null || region.isBlank()) {
            throw new IllegalStateException("商品画像専用のS3バケット、リージョン、フロントエンドのバケット名を設定してください。");
        }
        try {
            URI uri = URI.create(publicBaseUrl);
            if (!"https".equals(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)
                    || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) {
                throw new IllegalArgumentException();
            }
            String base = publicBaseUrl.replaceAll("/+$", "");
            // image_url は VARCHAR(255)。UUID 形式のキーも含めて収まる設定に限定する。
            if (base.length() + 51 > 255) throw new IllegalArgumentException();
            return base;
        } catch (RuntimeException e) {
            throw new IllegalStateException("画像配信元にはパス・クエリのないHTTPSのURLを設定してください。", e);
        }
    }
}
