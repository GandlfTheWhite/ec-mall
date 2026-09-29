package com.zyd.ecmall.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProductImageProperties.class)
public class ProductImageConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "app.product-images.enabled", havingValue = "true")
    S3Client productImageS3Client(ProductImageProperties properties) {
        properties.checkedBaseUrl();
        // AWS SDK の標準認証チェーンを使用。EB では EC2 インスタンスロールを利用する。
        return S3Client.builder().region(Region.of(properties.region()))
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(10)))
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(10)))
                .build();
    }
}
