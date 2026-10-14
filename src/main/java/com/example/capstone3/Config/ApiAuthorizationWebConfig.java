package com.example.capstone3.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ApiAuthorizationWebConfig implements WebMvcConfigurer {
    private final ApiAuthorizationInterceptor authorization;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorization).addPathPatterns("/api/v1/**");
    }
}
