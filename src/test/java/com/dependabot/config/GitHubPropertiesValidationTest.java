package com.dependabot.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GitHubProperties validation
 * Uses ApplicationContextRunner to test context loading failures
 */
@DisplayName("GitHub Properties Validation Tests")
class GitHubPropertiesValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Configuration
    @EnableConfigurationProperties(GitHubProperties.class)
    static class TestConfiguration {
    }

    @Test
    @DisplayName("Should fail when GitHub App ID is missing")
    void shouldFailWhenAppIdMissing() {
        contextRunner
                .withPropertyValues(
                        "github.installation-id=test-install",
                        "github.private-key=test-key",
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .hasMessageContaining("appId");
                });
    }

    @Test
    @DisplayName("Should fail when GitHub Installation ID is missing")
    void shouldFailWhenInstallationIdMissing() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.private-key=test-key",
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .hasMessageContaining("installationId");
                });
    }

    @Test
    @DisplayName("Should fail when GitHub Private Key is missing")
    void shouldFailWhenPrivateKeyMissing() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .hasMessageContaining("privateKey");
                });
    }

    @Test
    @DisplayName("Should fail when GitHub Owner is missing")
    void shouldFailWhenOwnerMissing() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.private-key=test-key",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .hasMessageContaining("owner");
                });
    }

    @Test
    @DisplayName("Should succeed with all required properties")
    void shouldSucceedWithAllRequiredProperties() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.private-key=test-key",
                        "github.owner=test-owner",
                        "github.repos=repo1,repo2"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GitHubProperties.class);

                    GitHubProperties props = context.getBean(GitHubProperties.class);
                    assertThat(props.getAppId()).isEqualTo("test-app");
                    assertThat(props.getInstallationId()).isEqualTo("test-install");
                    assertThat(props.getPrivateKey()).isEqualTo("test-key");
                    assertThat(props.getOwner()).isEqualTo("test-owner");
                    assertThat(props.getRepos()).containsExactly("repo1", "repo2");
                });
    }

    @Test
    @DisplayName("Should handle empty string as invalid")
    void shouldRejectEmptyStrings() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=",
                        "github.installation-id=test-install",
                        "github.private-key=test-key",
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> assertThat(context).hasFailed());
    }
}