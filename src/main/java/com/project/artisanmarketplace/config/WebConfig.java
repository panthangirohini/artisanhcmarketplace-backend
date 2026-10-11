package com.project.artisanmarketplace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Serve uploaded product images at /uploads/<file> (ProductController saves them to ./uploads)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get("uploads").toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
