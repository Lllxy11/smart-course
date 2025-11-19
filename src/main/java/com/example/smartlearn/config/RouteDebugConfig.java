package com.example.smartlearn.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@Configuration
public class RouteDebugConfig {

    @Bean
    public CommandLineRunner showRoutes(RequestMappingHandlerMapping handlerMapping) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("📋 所有注册的 API 路由：");
            System.out.println("========================================");

            Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();

            if (map.isEmpty()) {
                System.out.println("⚠️ 警告：没有找到任何路由！");
            } else {
                map.forEach((info, method) -> {
                    System.out.println(info + " -> " + method);
                });
            }

            System.out.println("========================================\n");
        };
    }
}