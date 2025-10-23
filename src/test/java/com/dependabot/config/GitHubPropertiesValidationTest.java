package com.dependabot.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    @DisplayName("Should succeed with all required properties and direct private key")
    void shouldSucceedWithPrivateKey() {
        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.private-key=test-key-content",
                        "github.owner=test-owner",
                        "github.repos=repo1,repo2"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GitHubProperties.class);

                    GitHubProperties props = context.getBean(GitHubProperties.class);
                    assertThat(props.getAppId()).isEqualTo("test-app");
                    assertThat(props.getInstallationId()).isEqualTo("test-install");
                    assertThat(props.getPrivateKey()).isEqualTo("test-key-content");
                    assertThat(props.getOwner()).isEqualTo("test-owner");
                    assertThat(props.getRepos()).containsExactly("repo1", "repo2");

                    // Verify getPrivateKeyContent() returns the direct key
                    assertThat(props.getPrivateKeyContent()).isEqualTo("test-key-content");
                });
    }

    @Test
    @DisplayName("Should succeed with private key file path provided")
    void shouldSucceedWithPrivateKeyFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary key file directly in tempDir (not in subdirectories)
        Path keyFile = tempDir.resolve("test-key.pem");
        String keyContent = "-----BEGIN RSA PRIVATE KEY-----\ntest-key-data\n-----END RSA PRIVATE KEY-----";
        Files.writeString(keyFile, keyContent);

        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.private-key-file=" + keyFile.toString(),
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GitHubProperties.class);

                    GitHubProperties props = context.getBean(GitHubProperties.class);
                    assertThat(props.getPrivateKeyFile()).isEqualTo(keyFile.toString());

                    // Verify getPrivateKeyContent() reads from file
                    assertThat(props.getPrivateKeyContent()).isEqualTo(keyContent);
                });
    }

    @Test
    @DisplayName("Should prefer direct private key over file when both provided")
    void shouldPreferDirectKeyOverFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary key file directly in tempDir
        Path keyFile = tempDir.resolve("test-key.pem");
        Files.writeString(keyFile, "file-key-content");

        contextRunner
                .withPropertyValues(
                        "github.app-id=test-app",
                        "github.installation-id=test-install",
                        "github.private-key=direct-key-content",
                        "github.private-key-file=" + keyFile.toString(),
                        "github.owner=test-owner",
                        "github.repos=repo1"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GitHubProperties.class);

                    GitHubProperties props = context.getBean(GitHubProperties.class);

                    // Should use direct key, not file
                    assertThat(props.getPrivateKeyContent()).isEqualTo("direct-key-content");
                });
    }

    @Test
    @DisplayName("Should handle empty string as invalid for required fields")
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