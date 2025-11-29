package com.documentpreview.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for setting up CORS, static resources, and other web-related settings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configures CORS mappings for the application.
     * 
     * @param registry The CORS registry to configure.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("ETag", "Last-Modified")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * Configures resource handlers for serving static resources.
     * 
     * @param registry The resource handler registry to configure.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources from src/main/resources/static
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        // Serve index.html for all non-API requests to support SPA routing
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/index.html");
    }
}