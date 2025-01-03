package com.nyc.boards.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private String resourcePath = "/upload/**"; // View에서 사용할 경로
    private String savePath = "file:///home/silkwave/apps/boards/spring_upload_files/"; // 실제 파일 저장 경로
    // private String savePath = "file:///C:/development/intellij_community/spring_upload_files/";

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Register resource handler for uploaded files
        registry.addResourceHandler(resourcePath)
                .addResourceLocations(savePath);

        // Register resource handler for favicon.ico
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }
}
