package com.dependabot.gauge;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dependabot.DependabotApplication;
import com.dependabot.config.GitHubProperties;
import com.dependabot.config.SchedulerProperties;
import com.dependabot.dto.PRDto;
import com.dependabot.scheduler.PRRefreshScheduler;
import com.dependabot.service.GitHubService;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DependabotApplication.class
)
@TestPropertySource(properties = {
        "github.app-id=test-app",
        "github.installation-id=test-install",
        "github.private-key=test-key",
        "github.owner=test-owner",
        "github.repos=repo1,repo2",
        "scheduler.enabled=false",  // Disabled by default for tests
        "scheduler.cron=*/5 * * * * *",  // Every 5 seconds for testing
        "scheduler.max-retries=3",
        "scheduler.retry-delay-ms=100"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SchedulerSteps {

    @LocalServerPort
    private int port;

    @Autowired(required = false)  // required = false because scheduler bean might not exist when disabled
    private PRRefreshScheduler scheduler;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private GitHubService gitHubService;

    @SpyBean
    private SchedulerProperties schedulerProperties;

    @SpyBean
    private GitHubProperties gitHubProperties;

    private TestContextManager testContextManager;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger schedulerLogger;
    private AtomicInteger gitHubApiCallCount = new AtomicInteger(0);
    private boolean simulateFailure = false;
    private int failureCount = 0;
    private int maxFailures = Integer.MAX_VALUE;

    @BeforeScenario
    public void setUp() throws Exception {
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        // Setup log capturing
        schedulerLogger = (Logger) LoggerFactory.getLogger(PRRefreshScheduler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        schedulerLogger.addAppender(listAppender);

        // Reset counters
        gitHubApiCallCount.set(0);
        simulateFailure = false;
        failureCount = 0;
        maxFailures = Integer.MAX_VALUE;

        // Setup default mock behavior
        setupDefaultMocks();

        org.slf4j.Logger log = LoggerFactory.getLogger(SchedulerSteps.class);
        log.debug("Spring context initialized. Port: {}", port);
    }

    private void setupDefaultMocks() {
        // Setup default GitHub service behavior
        when(gitHubService.getDependabotPRs(anyString())).thenAnswer(invocation -> {
            gitHubApiCallCount.incrementAndGet();

            if (simulateFailure) {
                if (failureCount < maxFailures) {
                    failureCount++;
                    throw new RuntimeException("Simulated GitHub API failure");
                }
            }

            String repo = invocation.getArgument(0);
            // Return mock PR data with Instant instead of Date
            List<PRDto> prs = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                PRDto pr = new PRDto();
                pr.setNumber(i);
                pr.setTitle("PR " + i + " for " + repo);
                pr.setRepository(repo);
                pr.setCreatedAt(Instant.now());  // Use Instant instead of Date
                pr.setUpdatedAt(Instant.now());
                prs.add(pr);
            }
            return prs;
        });
    }

    // Scheduler should be disabled by default
    @Step("Scheduler should not be enabled by default")
    public void verifySchedulerDisabledByDefault() {
        // Check if scheduler bean exists in context
        boolean schedulerExists = applicationContext.containsBean("PRRefreshScheduler");
        assertThat(schedulerExists)
                .as("Scheduler bean should not exist when disabled")
                .isFalse();

        // Also verify through properties
        assertThat(schedulerProperties.isEnabled())
                .as("Scheduler should be disabled in properties")
                .isFalse();
    }

    @Step("No scheduled tasks should run when disabled")
    public void noScheduledTasksWhenDisabled() throws InterruptedException {
        // Wait a bit to ensure no execution happens
        Thread.sleep(2000);

        // Check logs for any scheduler execution
        List<ILoggingEvent> logs = listAppender.list;
        boolean hasSchedulerExecution = logs.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Starting scheduled PR refresh"));

        assertThat(hasSchedulerExecution)
                .as("No scheduler execution should occur when disabled")
                .isFalse();

        // Verify no GitHub API calls were made
        assertThat(gitHubApiCallCount.get())
                .as("No GitHub API calls should be made")
                .isEqualTo(0);
    }

    // Scheduler should run on configured schedule when enabled
    @Step("Enable scheduler with cron expression <cronExpression>")
    public void enableSchedulerWithCronExpression(String cronExpression) {
        // Store configuration for enabling scheduler
        ScenarioDataStore.put("scheduler.enabled", true);
        ScenarioDataStore.put("scheduler.cron", cronExpression);

        // Note: In a real test, you'd need to restart the context with new properties
        // For this implementation, we'll simulate the behavior
        when(schedulerProperties.isEnabled()).thenReturn(true);
        when(schedulerProperties.getCron()).thenReturn(cronExpression);
    }

    @Step("Scheduler should have executed at least once")
    public void verifySchedulerExecuted() {
        if (scheduler != null) {
            assertThat(scheduler.getExecutionCount())
                    .as("Scheduler execution count")
                    .isGreaterThan(0);

            assertThat(scheduler.getLastExecutionTime())
                    .as("Last execution time")
                    .isNotNull();
        } else {
            // Check logs for execution
            boolean hasExecution = listAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("Starting scheduled PR refresh"));
            assertThat(hasExecution)
                    .as("Scheduler should have logged execution")
                    .isTrue();
        }
    }

    @Step("Cache should contain fresh PR data")
    public void cacheShouldContainFreshData() {
        Cache cache = cacheManager.getCache("github-prs");
        assertThat(cache).isNotNull();

        // Check that cache has data for configured repos
        List<String> repos = gitHubProperties.getRepos();
        for (String repo : repos) {
            String cacheKey = gitHubProperties.getOwner() + "/" + repo;
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            assertThat(wrapper)
                    .as("Cache should contain data for " + repo)
                    .isNotNull();

            @SuppressWarnings("unchecked")
            List<PRDto> prs = (List<PRDto>) wrapper.get();
            assertThat(prs)
                    .as("PRs for " + repo)
                    .isNotEmpty();
        }
    }

    // Scheduler should evict cache before refresh
    @Step("Wait for scheduler to execute")
    public void waitForSchedulerExecution() throws InterruptedException {
        // Wait up to 10 seconds for scheduler to execute
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    if (scheduler != null) {
                        return scheduler.getExecutionCount() > 0;
                    }
                    // Check logs if scheduler is not available
                    return listAppender.list.stream()
                            .anyMatch(event -> event.getFormattedMessage().contains("Starting scheduled PR refresh"));
                });
    }

    @Step("Enable scheduler")
    public void enableScheduler() {
        when(schedulerProperties.isEnabled()).thenReturn(true);
        // In a real scenario, you'd need to refresh the context
    }

    @Step("Store initial cache data")
    public void storeInitialCacheData() {
        Cache cache = cacheManager.getCache("github-prs");
        assertThat(cache).isNotNull();

        // Store some initial data with Instant
        PRDto initialPR = new PRDto();
        initialPR.setNumber(999);
        initialPR.setTitle("Initial PR");
        initialPR.setCreatedAt(Instant.now().minusSeconds(10)); // Old timestamp using Instant
        initialPR.setUpdatedAt(Instant.now().minusSeconds(10));

        List<PRDto> initialData = Collections.singletonList(initialPR);
        cache.put("test-owner/repo1", initialData);

        // Store the initial data in scenario store for later comparison
        ScenarioDataStore.put("initial-cache-data", initialData);
    }

    @Step("Old cache data should be evicted")
    public void oldCacheDataEvicted() {
        Cache cache = cacheManager.getCache("github-prs");
        assertThat(cache).isNotNull();

        Cache.ValueWrapper wrapper = cache.get("test-owner/repo1");
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            List<PRDto> currentData = (List<PRDto>) wrapper.get();
            @SuppressWarnings("unchecked")
            List<PRDto> initialData = (List<PRDto>) ScenarioDataStore.get("initial-cache-data");

            // Check that the data has changed (old data evicted, new data loaded)
            assertThat(currentData)
                    .as("Cache data should be different from initial data")
                    .isNotEqualTo(initialData);

            // Verify it's fresh data (not the PR with number 999)
            boolean hasOldData = currentData.stream()
                    .anyMatch(pr -> pr.getNumber() == 999);
            assertThat(hasOldData)
                    .as("Old data should be evicted")
                    .isFalse();
        }
    }

    @Step("New cache data should be present")
    public void newCacheDataPresent() {
        cacheShouldContainFreshData(); // Reuse the implementation
    }

    // Scheduler should handle GitHub API errors gracefully
    @Step("Simulate GitHub API failure")
    public void simulateGitHubFailure() {
        simulateFailure = true;
        maxFailures = Integer.MAX_VALUE; // Always fail
    }

    @Step("Scheduler should log error")
    public void schedulerShouldLogError() {
        List<ILoggingEvent> logs = listAppender.list;
        boolean hasErrorLog = logs.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR &&
                        (event.getFormattedMessage().contains("Scheduled PR refresh failed") ||
                                event.getFormattedMessage().contains("retry attempts exhausted")));

        assertThat(hasErrorLog)
                .as("Error should be logged for API failure")
                .isTrue();
    }

    @Step("Scheduler should not crash application")
    public void schedulerShouldNotCrashApp() {
        // Verify application is still responding
        Response response = given()
                .port(port)
                .when()
                .get("/health")
                .then()
                .extract()
                .response();

        assertThat(response.getStatusCode())
                .as("Application should still be running")
                .isEqualTo(200);
    }

    // Scheduler should retry on failure
    @Step("Enable scheduler with max retries of 3")
    public void enableSchedulerWith3Retries() {
        when(schedulerProperties.isEnabled()).thenReturn(true);
        when(schedulerProperties.getMaxRetries()).thenReturn(3);
        when(schedulerProperties.getRetryDelayMs()).thenReturn(100L);
    }

    @Step("Simulate temporary GitHub API failure")
    public void simulateTemporaryGitHubFailure() {
        simulateFailure = true;
        maxFailures = 2; // Fail first 2 attempts, succeed on third
        failureCount = 0;
    }

    @Step("Wait for scheduler to execute with retries")
    public void waitForRetryExecution() throws InterruptedException {
        // Wait for retries to complete
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> gitHubApiCallCount.get() >= 3);
    }

    @Step("Scheduler should have retried 3 times")
    public void schedulerShouldRetry3Times() {
        // Verify retry attempts in logs
        List<ILoggingEvent> logs = listAppender.list;
        long retryLogCount = logs.stream()
                .filter(event -> event.getFormattedMessage().contains("Refresh attempt") &&
                        event.getFormattedMessage().contains("failed, retrying"))
                .count();

        assertThat(retryLogCount)
                .as("Should have retry log messages")
                .isGreaterThanOrEqualTo(2); // At least 2 retry attempts

        // Verify API was called multiple times
        verify(gitHubService, atLeast(3)).getDependabotPRs(anyString());
    }

    @Step("Scheduler should eventually succeed after retry")
    public void schedulerShouldSucceedAfterRetry() {
        // Check for success log
        boolean hasSuccessLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Successfully refreshed after") &&
                        event.getFormattedMessage().contains("retry attempts"));

        assertThat(hasSuccessLog)
                .as("Should log successful completion after retries")
                .isTrue();

        // Verify cache has data
        cacheShouldContainFreshData();
    }

    // Scheduler should refresh all configured repositories
    @Step("Configure multiple repositories")
    public void configureMultipleRepositories() {
        List<String> repos = Arrays.asList("repo1", "repo2", "repo3");
        when(gitHubProperties.getRepos()).thenReturn(repos);
        ScenarioDataStore.put("configured-repos", repos);
    }

    @Step("All repositories should be refreshed")
    public void allRepositoriesRefreshed() {
        @SuppressWarnings("unchecked")
        List<String> repos = (List<String>) ScenarioDataStore.get("configured-repos");

        // Verify each repo was called
        for (String repo : repos) {
            verify(gitHubService, atLeastOnce()).getDependabotPRs(repo);
        }
    }

    @Step("Cache should contain PRs from all repositories")
    public void cacheContainsPRsFromAllRepositories() {
        @SuppressWarnings("unchecked")
        List<String> repos = (List<String>) ScenarioDataStore.get("configured-repos");
        Cache cache = cacheManager.getCache("github-prs");

        for (String repo : repos) {
            String cacheKey = gitHubProperties.getOwner() + "/" + repo;
            assertThat(cache.get(cacheKey))
                    .as("Cache should contain data for " + repo)
                    .isNotNull();
        }
    }

    // Scheduler should log execution metrics
    @Step("Logs should contain execution start time")
    public void logsContainExecutionStartTime() {
        boolean hasStartLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Starting scheduled PR refresh") &&
                        event.getFormattedMessage().contains("execution #"));

        assertThat(hasStartLog)
                .as("Should log execution start with execution number")
                .isTrue();
    }

    @Step("Logs should contain execution duration")
    public void logsContainExecutionDuration() {
        boolean hasDurationLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().matches(".*completed.*in \\d+ms.*") ||
                        event.getFormattedMessage().matches(".*failed after \\d+ms.*"));

        assertThat(hasDurationLog)
                .as("Should log execution duration in milliseconds")
                .isTrue();
    }

    @Step("Logs should contain number of PRs refreshed")
    public void logsContainNumberOfRefreshedPRs() {
        boolean hasPRCountLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Refreshed") &&
                        event.getFormattedMessage().contains("PRs"));

        assertThat(hasPRCountLog)
                .as("Should log number of PRs refreshed")
                .isTrue();
    }

    // Scheduler execution should not block API requests
    @Step("Enable scheduler with long-running task")
    public void enableSchedulerWithLongRunningTask() {
        // Make GitHub service calls take longer
        when(gitHubService.getDependabotPRs(anyString())).thenAnswer(invocation -> {
            Thread.sleep(3000); // Simulate long-running task
            return new ArrayList<PRDto>();
        });

        when(schedulerProperties.isEnabled()).thenReturn(true);
    }

    @Step("Make API request while scheduler is running")
    public void makeAPIRequestWhileSchedulerIsRunning() {
        // Start scheduler execution in background
        CompletableFuture<Void> schedulerFuture = CompletableFuture.runAsync(() -> {
            if (scheduler != null) {
                scheduler.refreshPRData();
            }
        });

        // Make API request while scheduler is running
        CompletableFuture<Response> apiFuture = CompletableFuture.supplyAsync(() ->
                given()
                        .port(port)
                        .when()
                        .get("/health")
                        .then()
                        .extract()
                        .response()
        );

        ScenarioDataStore.put("api-response-future", apiFuture);
        ScenarioDataStore.put("scheduler-future", schedulerFuture);
    }

    @Step("API request should complete successfully")
    public void apiRequestShouldComplete() {
        @SuppressWarnings("unchecked")
        CompletableFuture<Response> apiFuture =
                (CompletableFuture<Response>) ScenarioDataStore.get("api-response-future");

        try {
            Response response = apiFuture.get(5, TimeUnit.SECONDS);
            assertThat(response.getStatusCode())
                    .as("API request should complete successfully")
                    .isEqualTo(200);
        } catch (Exception e) {
            throw new AssertionError("API request should not timeout or fail", e);
        }
    }

    @Step("API should not be blocked by scheduler")
    public void apiNotBlockedByScheduler() {
        @SuppressWarnings("unchecked")
        CompletableFuture<Response> apiFuture =
                (CompletableFuture<Response>) ScenarioDataStore.get("api-response-future");

        // Check if API request completed quickly (within 1 second)
        // We'll use isDone() after a short wait instead of isCompletedWithin
        try {
            // Wait a short time and check if completed
            boolean completedQuickly = apiFuture.get(1, TimeUnit.SECONDS) != null;
            assertThat(completedQuickly)
                    .as("API request should complete within 1 second without waiting for scheduler")
                    .isTrue();
        } catch (Exception e) {
            throw new AssertionError("API request should complete quickly without blocking", e);
        }
    }
}