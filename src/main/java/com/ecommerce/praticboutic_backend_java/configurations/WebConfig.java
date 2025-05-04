package com.ecommerce.praticboutic_backend_java.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.root.url.front}")
    private String fronturl;

    @Value("${file.upload.location}")
    private String upLoadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all endpoints
                .allowedOrigins(fronturl)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .exposedHeaders("Content-Length", "X-JSON") // Optional, expose specific headers
                .allowedHeaders("Content-Type", "Authorization", "Accept", "Accept-Language", "X-Authorization")
                .maxAge(86400)
                .allowCredentials(true); // Allow cookies/authentication headers
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/upload/**")
                .addResourceLocations(upLoadPath);
        // Par ex. : "file:/home/fred/uploads/"
    }


}