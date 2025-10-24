package com.dependabot.scheduler;

import com.dependabot.config.GitHubProperties;
import com.dependabot.config.SchedulerProperties;
import com.dependabot.dto.PRDto;
import com.dependabot.service.GitHubService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Scheduled task to refresh Dependabot PR data from GitHub
 * Runs on a configurable cron schedule and evicts cache before refresh
 */
@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class PRRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(PRRefreshScheduler.class);

    private final GitHubService gitHubService;
    private final CacheManager cacheManager;
    private final GitHubProperties gitHubProperties;
    private final SchedulerProperties schedulerProperties;

    /**
     * -- GETTER --
     *  Get the number of times the scheduler has executed
     * return execution count
     */
    @Getter
    private int executionCount = 0;
    /**
     * -- GETTER --
     *  Get the last execution time
     * return last execution instant
     */
    @Getter
    private Instant lastExecutionTime;
    /**
     * -- GETTER --
     *  Get the duration of the last execution
     * return last execution duration
     */
    @Getter
    private Duration lastExecutionDuration;

    public PRRefreshScheduler(
            GitHubService gitHubService,
            CacheManager cacheManager,
            GitHubProperties gitHubProperties,
            SchedulerProperties schedulerProperties) {
        this.gitHubService = gitHubService;
        this.cacheManager = cacheManager;
        this.gitHubProperties = gitHubProperties;
        this.schedulerProperties = schedulerProperties;

        log.info("PR Refresh Scheduler initialized with cron: {}", schedulerProperties.getCron());
    }

    /**
     * Scheduled task to refresh PR data
     * Configured via scheduler.cron property
     */
    @Scheduled(cron = "${scheduler.cron}")
    public void refreshPRData() {
        Instant startTime = Instant.now();
        log.info("=== Starting scheduled PR refresh (execution #{}) ===", ++executionCount);

        try {
            // Evict cache before refresh
            evictCache();

            // Refresh data for all configured repositories
            int totalPRs = refreshAllRepositories();

            // Record metrics
            Duration duration = Duration.between(startTime, Instant.now());
            lastExecutionTime = startTime;
            lastExecutionDuration = duration;

            log.info("=== Scheduled PR refresh completed successfully in {}ms. Refreshed {} PRs ===",
                    duration.toMillis(), totalPRs);

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            log.error("=== Scheduled PR refresh failed after {}ms (execution #{}) ===",
                    duration.toMillis(), executionCount, e);

            // Don't rethrow - let scheduler continue running
        }
    }

    /**
     * Refresh data for all configured repositories with retry logic
     *
     * @return total number of PRs refreshed
     */
    private int refreshAllRepositories() {
        List<String> repositories = gitHubProperties.getRepos();
        log.debug("Refreshing {} repositories: {}", repositories.size(), repositories);

        int totalPRs = 0;
        int attempts = 0;
        Exception lastException = null;

        while (attempts <= schedulerProperties.getMaxRetries()) {
            try {
                for (String repo : repositories) {
                    log.debug("Fetching PRs for repository: {}", repo);
                    List<PRDto> prs = gitHubService.getDependabotPRs(repo);
                    totalPRs += prs.size();
                    log.debug("Found {} PRs for {}", prs.size(), repo);
                }

                // Success - break retry loop
                if (attempts > 0) {
                    log.info("Successfully refreshed after {} retry attempts", attempts);
                }
                return totalPRs;

            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (attempts <= schedulerProperties.getMaxRetries()) {
                    log.warn("Refresh attempt {} failed, retrying in {}ms...",
                            attempts, schedulerProperties.getRetryDelayMs(), e);

                    try {
                        Thread.sleep(schedulerProperties.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("All {} retry attempts exhausted", schedulerProperties.getMaxRetries());
                }
            }
        }

        // All retries failed
        throw new RuntimeException(
                "Failed to refresh repositories after " + schedulerProperties.getMaxRetries() + " retries",
                lastException);
    }

    /**
     * Evict all cached PR data before refresh
     */
    private void evictCache() {
        log.debug("Evicting cache before refresh");

        var cache = cacheManager.getCache("github-prs");
        if (cache != null) {
            cache.clear();
            log.debug("Cache evicted successfully");
        } else {
            log.warn("Cache 'github-prs' not found");
        }
    }

}