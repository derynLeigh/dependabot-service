package com.dependabot.gauge;

import com.dependabot.config.GitHubProperties;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gauge step implementations for Configuration scenarios
 */
@SpringBootTest(
        classes = com.dependabot.DependabotApplication.class
)
@EnableConfigurationProperties(GitHubProperties.class)
@TestPropertySource(properties = {
        "github.app-id=test-app-123",
        "github.installation-id=test-install-456",
        "github.private-key=test-private-key",
        "github.owner=test-owner",
        "github.repos=repo1,repo2,repo3"
})
public class ConfigurationSteps {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationSteps.class);

    @Autowired
    private GitHubProperties gitHubProperties;

    private TestContextManager testContextManager;

    @BeforeScenario
    public void setUp() throws Exception {
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        log.debug("Configuration test context initialized");
    }

    @Step("Start the application with valid GitHub configuration")
    public void startApplicationWithValidConfig() {
        assertThat(gitHubProperties).isNotNull();
        log.debug("Application started with valid configuration");
    }

    @Step("GitHub App ID should be loaded correctly")
    public void verifyAppIdLoaded() {
        assertThat(gitHubProperties.getAppId())
                .as("GitHub App ID")
                .isNotNull()
                .isEqualTo("test-app-123");
    }

    @Step("GitHub Installation ID should be loaded correctly")
    public void verifyInstallationIdLoaded() {
        assertThat(gitHubProperties.getInstallationId())
                .as("GitHub Installation ID")
                .isNotNull()
                .isEqualTo("test-install-456");
    }

    @Step("GitHub Owner should be loaded correctly")
    public void verifyOwnerLoaded() {
        assertThat(gitHubProperties.getOwner())
                .as("GitHub Owner")
                .isNotNull()
                .isEqualTo("test-owner");
    }

    @Step("GitHub Repositories list should be loaded correctly")
    public void verifyReposLoaded() {
        List<String> repos = gitHubProperties.getRepos();

        assertThat(repos)
                .as("GitHub Repositories")
                .isNotNull()
                .hasSize(3)
                .containsExactly("repo1", "repo2", "repo3");
    }

    @Step("Start the application with only required GitHub configuration")
    public void startWithRequiredConfigOnly() {
        assertThat(gitHubProperties).isNotNull();
    }

    @Step("Default GitHub Owner should be <defaultOwner>")
    public void verifyDefaultOwner(String defaultOwner) {
        assertThat(gitHubProperties.getOwner())
                .as("GitHub Owner (default or configured)")
                .isEqualTo(defaultOwner);
    }

    @Step("Default repositories list should contain default repositories")
    public void verifyDefaultRepos() {
        List<String> repos = gitHubProperties.getRepos();

        assertThat(repos)
                .as("Repositories list")
                .isNotNull()
                .isNotEmpty();
    }
}