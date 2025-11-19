package com.example.smartlearn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")      // 允许所有源（生产环境应限制）
                .allowedMethods("*")      // 允许所有HTTP方法
                .allowedHeaders("*");     // 允许所有请求头
    }

}