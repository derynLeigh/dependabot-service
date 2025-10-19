package com.dependabot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine
 * Caches GitHub API responses to reduce API calls and improve performance
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.ttl-ms:300000}")  // Default 5 minutes
    private long cacheTtlMs;

    /**
     * Configure Caffeine cache manager with TTL
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("github-prs");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheTtlMs, TimeUnit.MILLISECONDS)
                .maximumSize(100)  // Maximum 100 entries
                .recordStats());   // Enable statistics

        return cacheManager;
    }
}