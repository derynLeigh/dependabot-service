package com.dependabot.scheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import java.util.List;
import com.dependabot.config.GitHubProperties;
import com.dependabot.config.SchedulerProperties;
import com.dependabot.dto.PRDto;
import com.dependabot.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
        schedulerProperties.setRetryDelayMs(10);  // Set small delay as default for tests

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

    @Test
    @DisplayName("Should retry on failure up to max retries")
    void shouldRetryOnFailure() {
        // Arrange
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("github-prs")).thenReturn(cache);

        // Simulate failures that should trigger retries
        when(gitHubService.getDependabotPRs("repo1"))
                .thenThrow(new RuntimeException("API Error"));

        // Act
        prRefreshScheduler.refreshPRData();

        // Assert
        // Should be called 4 times (initial + 3 retries)
        verify(gitHubService, times(4)).getDependabotPRs("repo1");
        verify(cache).clear(); // Cache should still be cleared
    }

    @Test
    @DisplayName("Should log error when all retry attempts are exhausted")
    void shouldLogErrorWhenAllRetriesExhausted() {
        // Arrange
        // Set up log capturing
        Logger logger = (Logger) LoggerFactory.getLogger(PRRefreshScheduler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("github-prs")).thenReturn(cache);
        when(schedulerProperties.getMaxRetries()).thenReturn(3);

        // Simulate persistent failures
        when(gitHubService.getDependabotPRs("repo1"))
                .thenThrow(new RuntimeException("API Error"));

        // Act
        prRefreshScheduler.refreshPRData();

        // Assert
        List<ILoggingEvent> logsList = listAppender.list;

        // Verify the "All retry attempts exhausted" log message
        boolean foundExhaustedLog = logsList.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("All 3 retry attempts exhausted"));
        assertThat(foundExhaustedLog)
                .as("Should log error about exhausted retries")
                .isTrue();

        // Verify the final failure log from refreshPRData
        boolean foundFailureLog = logsList.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("Scheduled PR refresh failed after"));
        assertThat(foundFailureLog)
                .as("Should log error about scheduler refresh failure")
                .isTrue();

        // Verify number of retry attempts
        verify(gitHubService, times(4)).getDependabotPRs("repo1");

        // Clean up
        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("Should succeed after retries when service recovers")
    void shouldSucceedAfterRetries() {
        // Arrange
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("github-prs")).thenReturn(cache);

        PRDto testPR = PRDto.builder()
                .number(456)
                .title("Test PR after retry")
                .repository("repo1")
                .author("dependabot[bot]")
                .url("https://github.com/test-owner/repo1/pull/456")
                .build();
        List<PRDto> testPRs = List.of(testPR);

        // Fail twice, then succeed on third attempt
        when(gitHubService.getDependabotPRs("repo1"))
                .thenThrow(new RuntimeException("Temporary failure"))
                .thenThrow(new RuntimeException("Another temporary failure"))
                .thenReturn(testPRs);

        when(gitHubService.getDependabotPRs("repo2")).thenReturn(Collections.emptyList());
        when(gitHubService.getDependabotPRs("repo3")).thenReturn(Collections.emptyList());

        // Act
        prRefreshScheduler.refreshPRData();

        // Assert
        // repo1 should be called 3 times (initial + 2 retries before success)
        verify(gitHubService, times(3)).getDependabotPRs("repo1");
        // Other repos should only be called once after retry success
        verify(gitHubService, times(1)).getDependabotPRs("repo2");
        verify(gitHubService, times(1)).getDependabotPRs("repo3");

        // Verify execution was recorded as successful
        assertThat(prRefreshScheduler.getExecutionCount()).isEqualTo(1);
        assertThat(prRefreshScheduler.getLastExecutionTime()).isNotNull();
        assertThat(prRefreshScheduler.getLastExecutionDuration()).isNotNull();
    }
}