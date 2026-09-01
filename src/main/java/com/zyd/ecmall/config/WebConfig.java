package com.zyd.ecmall.config;

import com.zyd.ecmall.interceptor.AdminAuthInterceptor;
import com.zyd.ecmall.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration(proxyBeanMethods = false)  // 🆕 ここを追加
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebConfig(JwtAuthInterceptor jwtAuthInterceptor, AdminAuthInterceptor adminAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.adminAuthInterceptor = adminAuthInterceptor;
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
                )
                .excludePathPatterns("/api/members");

        // 管理者用インターセプター
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}