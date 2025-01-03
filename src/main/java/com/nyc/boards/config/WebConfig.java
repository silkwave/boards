package com.nyc.boards.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String RESOURCE_PATH = "/upload/**"; // View에서 사용할 경로
    private static final String SAVE_PATH = "file:///home/silkwave/apps/boards/spring_upload_files/"; // 실제 파일 저장 경로

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 업로드된 파일에 대한 리소스 핸들러 등록
        registry.addResourceHandler(RESOURCE_PATH)
                .addResourceLocations(SAVE_PATH);
    }
}
