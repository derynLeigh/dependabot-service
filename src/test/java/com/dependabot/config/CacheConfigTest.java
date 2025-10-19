package com.dependabot.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Cache Configuration Tests")
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Should create Caffeine cache manager")
    void shouldCreateCacheManager() {
        assertThat(cacheManager)
                .as("Cache manager")
                .isNotNull()
                .isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    @DisplayName("Should have github-prs cache configured")
    void shouldHaveGitHubPRsCache() {
        assertThat(cacheManager.getCacheNames())
                .as("Cache names")
                .contains("github-prs");
    }

    @Test
    @DisplayName("Cache should be retrievable")
    void shouldRetrieveCache() {
        var cache = cacheManager.getCache("github-prs");

        assertThat(cache)
                .as("GitHub PRs cache")
                .isNotNull();
    }
}