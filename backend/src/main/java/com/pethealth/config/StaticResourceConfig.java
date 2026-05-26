package com.pethealth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射：将本地 uploads/ 目录暴露为 /files/** HTTP 路径
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${upload.base-dir:uploads}")
    private String baseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 始终相对于项目运行目录解析，保证跨电脑移植
        java.nio.file.Path workDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
        String absPath = workDir.resolve(baseDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absPath + File.separator);
    }
}
