package com.documentpreview.infra.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration class for setting up Caffeine cache managers.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Value("${app.cache.toon-ttl}")
    private long toonTtlSeconds;
    
    @Value("${app.cache.preview-ttl}")
    private long previewTtlSeconds;
    
    /**
     * Creates a Caffeine cache manager for TOON structures.
     * 
     * @return The Caffeine cache manager.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(toonTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }
    
    /**
     * Creates a Caffeine cache manager specifically for preview content.
     * 
     * @return The Caffeine cache manager for preview content.
     */
    @Bean
    public CacheManager previewCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("preview-content");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(previewTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(500)
                .recordStats());
        return cacheManager;
    }
}