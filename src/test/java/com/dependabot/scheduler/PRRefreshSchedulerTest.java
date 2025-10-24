package com.dependabot.scheduler;

import com.dependabot.config.GitHubProperties;
import com.dependabot.config.SchedulerProperties;
import com.dependabot.dto.PRDto;
import com.dependabot.scheduler.PRRefreshScheduler;
import com.dependabot.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PR Refresh Scheduler Tests")
class PRRefreshSchedulerTest {

    @Mock
    private GitHubService gitHubService;

    @Mock
    private CacheManager cacheManager;

    private GitHubProperties gitHubProperties;

    private SchedulerProperties schedulerProperties;

    private PRRefreshScheduler prRefreshScheduler;

    @BeforeEach
    void setUp() {
        // Initialize GitHub properties with test data
        gitHubProperties = new GitHubProperties();
        gitHubProperties.setOwner("test-owner");
        gitHubProperties.setRepos(List.of("repo1", "repo2", "repo3"));

        // Initialize scheduler properties with test values
        schedulerProperties = new SchedulerProperties();
        schedulerProperties.setEnabled(true);
        schedulerProperties.setCron("0 */30 * * * *");  // Every 30 minutes
        schedulerProperties.setMaxRetries(3);
        schedulerProperties.setRetryDelayMs(1000);

        // Initialize the PRRefreshScheduler with mocks and properties
        prRefreshScheduler = new PRRefreshScheduler(
                gitHubService,
                cacheManager,
                gitHubProperties,
                schedulerProperties
        );
    }

    @Test
    @DisplayName("Should evict cache before refresh")
    void shouldEvictCache() {
        // Arrange
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("github-prs")).thenReturn(cache);
        when(gitHubService.getDependabotPRs(any())).thenReturn(Collections.emptyList());

        // Act
        prRefreshScheduler.refreshPRData();

        // Assert
        verify(cache).clear();
    }

    @Test
    @DisplayName("Should refresh all configured repositories")
    void shouldRefreshAllRepositories() {
        // Arrange
        when(cacheManager.getCache("github-prs")).thenReturn(mock(Cache.class));
        when(gitHubService.getDependabotPRs(any())).thenReturn(Collections.emptyList());

        // Act
        prRefreshScheduler.refreshPRData();

        // Assert
        verify(gitHubService).getDependabotPRs("repo1");
        verify(gitHubService).getDependabotPRs("repo2");
        verify(gitHubService).getDependabotPRs("repo3");
    }
}