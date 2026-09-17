package com.zyd.ecmall.config;

import com.zyd.ecmall.interceptor.AdminAuthInterceptor;
import com.zyd.ecmall.interceptor.JwtAuthInterceptor;
import com.zyd.ecmall.interceptor.ResourceAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration(proxyBeanMethods = false)  // 🆕 ここを追加
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final ResourceAccessInterceptor resourceAccessInterceptor;

    public WebConfig(JwtAuthInterceptor jwtAuthInterceptor, AdminAuthInterceptor adminAuthInterceptor,
                     ResourceAccessInterceptor resourceAccessInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.resourceAccessInterceptor = resourceAccessInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ユーザー用 JWT インターセプター
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(
                        "/api/members/**",
                        "/api/auth/me",
                        "/api/products/**",
                        "/api/cart/**",
                        "/api/orders/**"
                );

        registry.addInterceptor(resourceAccessInterceptor)
                .addPathPatterns("/api/members/**", "/api/products/**");

        // 管理者用インターセプター
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
