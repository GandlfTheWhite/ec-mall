package com.zyd.ecmall.config;

import com.zyd.ecmall.interceptor.JwtAuthInterceptor;
import com.zyd.ecmall.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    public WebConfig(
            JwtAuthInterceptor jwtAuthInterceptor) {

        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(
                        "/api/members/**",
                        "/api/auth/me",
                        "/api/products/**",
                        "/api/cart/**",
                        "/api/orders/**"
                )
//              新規追加の場合、JWT認証はいらない↓パスを分けます。
                .excludePathPatterns(
                        "/api/members"
                );
    }

//    private final LoginInterceptor loginInterceptor;
//    public WebConfig(
//            LoginInterceptor loginInterceptor) {
//        this.loginInterceptor = loginInterceptor;
//    }
//    @Override
//    public void addInterceptors(
//            InterceptorRegistry registry) {
//
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/api/members/**");
//    }


}
