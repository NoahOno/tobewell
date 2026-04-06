package com.health.platform.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register Sa-Token interceptor
        registry.addInterceptor(new SaInterceptor(handle -> {
            // Check login for all routes except those below
            SaRouter.match("/**")
                .notMatch("/auth/login", "/auth/register", "/swagger-ui/**", "/v3/api-docs/**", "/", "/uploads/**")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
