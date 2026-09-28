package com.zyd.ecmall.config;

import com.zyd.ecmall.service.ProductImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.images", name = "enabled", havingValue = "true")
public class ImageStorageConfig {
    @Bean
    S3Client imageS3Client(@Value("${app.images.region}") String region) {
        if (region.isBlank()) throw new IllegalStateException("IMAGE_REGION is required for image upload");
        return S3Client.builder().region(Region.of(region)).build();
    }

    @Bean
    ProductImageStorage productImageStorage(S3Client imageS3Client,
            @Value("${app.images.bucket}") String bucket,
            @Value("${app.images.public-base-url}") String publicBaseUrl) {
        return new ProductImageStorage(imageS3Client, bucket, publicBaseUrl);
    }
}
