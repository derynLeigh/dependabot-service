package com.dependabot.gauge;

import com.dependabot.config.GitHubProperties;
import com.dependabot.dto.PRDto;
import com.dependabot.service.GitHubService;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContextManager;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Gauge step implementations for GitHub Service scenarios
 * Tests the GitHubService integration with mock/test data
 */
@SpringBootTest(
        classes = {
                com.dependabot.DependabotApplication.class,
        }
)
public class GitHubServiceSteps {

    private static final Logger log = LoggerFactory.getLogger(GitHubServiceSteps.class);

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private GitHubProperties gitHubProperties;

    private TestContextManager testContextManager;

    // Store state between steps
    private String jwtToken;
    private List<PRDto> pullRequests;
    private Exception lastException;

    @BeforeScenario
    public void setUp() throws Exception {
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        // Reset state
        jwtToken = null;
        pullRequests = null;
        lastException = null;

        log.debug("GitHub Service test context initialized");
    }

    @Step("Start the application with GitHub configuration")
    public void startApplicationWithGitHubConfig() {
        assertThat(gitHubService).isNotNull();
        assertThat(gitHubProperties).isNotNull();

        log.debug("Application started with GitHub configuration");
        log.debug("App ID: {}", gitHubProperties.getAppId());
        log.debug("Owner: {}", gitHubProperties.getOwner());
        log.debug("Repos: {}", gitHubProperties.getRepos());
    }

    @Step("GitHub service should be initialized")
    public void verifyGitHubServiceInitialized() {
        assertThat(gitHubService)
                .as("GitHub Service")
                .isNotNull();
    }

    @Step("GitHub service should generate valid JWT token")
    public void generateJWTToken() {
        jwtToken = gitHubService.generateJWT();

        log.debug("Generated JWT token: {}",
                jwtToken != null ? jwtToken.substring(0, Math.min(20, jwtToken.length())) + "..." : "null");

        assertThat(jwtToken)
                .as("JWT token")
                .isNotNull()
                .isNotEmpty();
    }

    @Step("JWT token should have three parts")
    public void verifyJWTTokenStructure() {
        assertThat(jwtToken)
                .as("JWT token should exist")
                .isNotNull();

        String[] parts = jwtToken.split("\\.");

        assertThat(parts)
                .as("JWT parts (header.payload.signature)")
                .hasSize(3);

        assertThat(parts[0])
                .as("JWT header")
                .isNotEmpty();

        assertThat(parts[1])
                .as("JWT payload")
                .isNotEmpty();

        assertThat(parts[2])
                .as("JWT signature")
                .isNotEmpty();
    }

    @Step("Get pull requests for repository <repositoryName>")
    public void getPullRequestsForRepository(String repositoryName) {
        try {
            log.debug("Fetching PRs for repository: {}", repositoryName);
            pullRequests = gitHubService.getDependabotPRs(repositoryName);
            log.debug("Retrieved {} PRs", pullRequests != null ? pullRequests.size() : 0);
        } catch (Exception e) {
            log.warn("Error fetching PRs: {}", e.getMessage());
            lastException = e;
            pullRequests = null;
        }
    }

    @Step("Get pull requests for repositories <repoList>")
    public void getPullRequestsForMultipleRepositories(String repoList) {
        try {
            List<String> repositories = Arrays.asList(repoList.split(","));
            log.debug("Fetching PRs for repositories: {}", repositories);

            pullRequests = gitHubService.getAllDependabotPRs(repositories);
            log.debug("Retrieved {} PRs from all repositories",
                    pullRequests != null ? pullRequests.size() : 0);
        } catch (Exception e) {
            log.warn("Error fetching PRs from multiple repos: {}", e.getMessage());
            lastException = e;
            pullRequests = null;
        }
    }

    @Step("Pull requests list should not be null")
    public void verifyPullRequestsNotNull() {
        assertThat(pullRequests)
                .as("Pull requests list")
                .isNotNull();
    }

    @Step("Pull requests list should be empty")
    public void verifyPullRequestsEmpty() {
        assertThat(pullRequests)
                .as("Pull requests list")
                .isNotNull()
                .isEmpty();
    }

    @Step("If pull requests exist, each should have required fields")
    public void verifyRequiredFieldsIfExist() {
        assertThat(pullRequests)
                .as("Pull requests list")
                .isNotNull();

        if (pullRequests.isEmpty()) {
            log.info("No PRs found - skipping field verification (this is acceptable)");
            return;
        }

        // Verify each PR has required fields
        for (PRDto pr : pullRequests) {
            assertThat(pr.getNumber())
                    .as("PR number")
                    .isNotNull();

            assertThat(pr.getTitle())
                    .as("PR title")
                    .isNotNull()
                    .isNotEmpty();

            assertThat(pr.getRepository())
                    .as("PR repository name")
                    .isNotNull()
                    .isNotEmpty();

            assertThat(pr.getUrl())
                    .as("PR URL")
                    .isNotNull()
                    .isNotEmpty();

            log.debug("✓ PR #{} - {} has all required fields", pr.getNumber(), pr.getTitle());
        }

        log.debug("✓ Verified all {} PRs have required fields", pullRequests.size());
    }

    @Step("If pull requests exist, all should be from Dependabot")
    public void verifyDependabotPRsIfExist() {
        assertThat(pullRequests)
                .as("Pull requests list")
                .isNotNull();

        if (pullRequests.isEmpty()) {
            log.info("No PRs found - skipping Dependabot verification (this is acceptable)");
            return;
        }

        // If there are PRs, verify they're from Dependabot
        for (PRDto pr : pullRequests) {
            assertThat(pr.getAuthor())
                    .as("PR #%d author should be Dependabot", pr.getNumber())
                    .containsIgnoringCase("dependabot");
        }

        log.debug("✓ Verified {} PRs are all from Dependabot", pullRequests.size());
    }

    @Step("Should handle repository not found error gracefully")
    public void verifyRepositoryNotFoundHandled() {
        // Either the service returned empty list or threw an exception
        // Both are valid ways to handle a non-existent repo

        if (lastException != null) {
            log.debug("Service threw exception (acceptable): {}", lastException.getMessage());
            assertThat(lastException.getMessage())
                    .as("Error message should be informative")
                    .isNotEmpty();
        } else {
            log.debug("Service returned empty list (acceptable)");
            assertThat(pullRequests)
                    .as("Pull requests list should be empty or null for non-existent repo")
                    .satisfiesAnyOf(
                            prs -> assertThat(prs).isNull(),
                            prs -> assertThat(prs).isEmpty()
                    );
        }
    }
}