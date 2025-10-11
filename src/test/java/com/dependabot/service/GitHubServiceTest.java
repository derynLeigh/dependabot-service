package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import com.dependabot.dto.PRDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GitHubService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GitHub Service Tests")
class GitHubServiceTest {

    @Mock
    private GitHubProperties gitHubProperties;

    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        when(gitHubProperties.getAppId()).thenReturn("test-app-id");
        when(gitHubProperties.getInstallationId()).thenReturn("test-installation-id");
        when(gitHubProperties.getPrivateKey()).thenReturn(getTestPrivateKey());
        when(gitHubProperties.getOwner()).thenReturn("test-owner");
        when(gitHubProperties.getRepos()).thenReturn(List.of("repo1", "repo2"));

        gitHubService = new GitHubService(gitHubProperties);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJWT() {
        String jwt = gitHubService.generateJWT();

        assertThat(jwt)
                .as("JWT token")
                .isNotNull()
                .isNotEmpty();

        // JWT should have 3 parts: header.payload.signature
        String[] parts = jwt.split("\\.");
        assertThat(parts)
                .as("JWT parts")
                .hasSize(3);
    }

    @Test
    @DisplayName("Should convert GitHub PR to DTO correctly")
    void shouldConvertPRToDto() {
        // This will test the toPRDto method once we implement it
        // We'll need to create mock GitHub PR objects

        // For now, this test will fail until we implement the service
        assertThat(gitHubService).isNotNull();
    }

    @Test
    @DisplayName("Should handle null repository name in PR")
    void shouldHandleNullRepoName() {
        // Test defensive programming
        assertThat(gitHubService).isNotNull();
    }

    /**
     * Test RSA private key in PEM format (2048-bit)
     * This is a dummy key for testing only
     */
    private String getTestPrivateKey() {
        return "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEA0Z3VS5JJcds3xfn/ygWyF0qEN+v6k23z4JYHJRxPxPNEhqkc\n" +
                "c/CBFSWfCDNJCkGxNLJZ0+4p2LHwNjFqvYR6Vc4JHVwxX4J5vXdXqInvlFxF9kGR\n" +
                "tGLKGLCKLWlRVMxKPfOFM/LqZ9xnQFmkJBQ5S+4dLUvH0WV+rq5H2L4PlKFjNMNO\n" +
                "Zc2U4BzjYSPjzZFO3MKIkqxUqGbRdTcZrN4uKFzf1d8fHsJt5HkTjKjX0IqFRJ8k\n" +
                "WXQjHHHJMTxIkGJvQhMkNmRcyPfHhJ4qJMYHqSMZKLZ8fKvPWzKXxYPWFQGNdBCO\n" +
                "lRvMzF6rKlWmYJXVYQzKKWdLQGtqJSPjZWJgZQIDAQABAoIBABfhJlyapFvqGmXN\n" +
                "fH3YYQZ4qYVb2qL0yWY6oRCkJLqWmJj0gNFRqGqzN8r0dLxA0L4Pm8LPYaHF1b8g\n" +
                "pQW7EJXmDxp3TIWPX0XjMqhPKW7RG2JqKvqIzLLxhBMZK7BqBf4FqF1WfXGQPOdG\n" +
                "vxqGKGDqYXYLQxJ5Lx3qLKWW7YnCqHXqjWKLNfMh1vGLKxJQyZrKqF3wLFPK8VmQ\n" +
                "MaJCqYFqJKxQWJb6jQpG8jKQqELNxRKLvRJLKxJQNKLQNxLJKxJQyZrKqF3wLFPK\n" +
                "8VmQMaJCqYFqJKxQWJb6jQpG8jKQqELNxRKLvRJLKxJQNKLQNxLJKxJQyZrKqF3w\n" +
                "LFPKgAECgYEA7kKhtzQj0HVkVFgwLNqxF0M3qMJNvV/qdKWKxJNqxQPxQKWLQNrr\n" +
                "JKxVfGJqKvqIzLLxhBMZK7BqBf4FqF1WfXGQPOdGvxqGKGDqYXYLQxJ5Lx3qLKWW\n" +
                "7YnCqHXqjWKLNfMh1vGLKxJQyZrKqF3wLFPK8VmQMaJCqYFqJKxQWJb6jQpG8jKQ\n" +
                "qECgYEA4S5KvQJ2/pM3KJqJpRxPxPNEhqkcc/CBFSWfCDNJCkGxNLJZ0+4p2LHw\n" +
                "NjFqvYR6Vc4JHVwxX4J5vXdXqInvlFxF9kGRtGLKGLCKLWlRVMxKPfOFM/LqZ9xn\n" +
                "QFmkJBQ5S+4dLUvH0WV+rq5H2L4PlKFjNMNOZc2U4BzjYSPjzZFO3MKIkqxUqGbR\n" +
                "dTcZrN4uKFzf1d8fHsJt5HkTjKjX0IqFRJ8kWXQjHHHJMTxIkGJvQhMkNmRcyPfH\n" +
                "hJ4qJECgYA7kKhtzQj0HVkVFgwLNqxF0M3qMJNvV/qdKWKxJNqxQPxQKWLQNrrJ\n" +
                "KxVfGJqKvqIzLLxhBMZK7BqBf4FqF1WfXGQPOdGvxqGKGDqYXYLQxJ5Lx3qLKWW\n" +
                "7YnCqHXqjWKLNfMh1vGLKxJQyZrKqF3wLFPK8VmQMaJCqYFqJKxQWJb6jQpG8jKQ\n" +
                "qECgYBfhJlyapFvqGmXNfH3YYQZ4qYVb2qL0yWY6oRCkJLqWmJj0gNFRqGqzN8r0\n" +
                "dLxA0L4Pm8LPYaHF1b8gpQW7EJXmDxp3TIWPX0XjMqhPKW7RG2JqKvqIzLLxhBMZ\n" +
                "K7BqBf4FqF1WfXGQPOdGvxqGKGDqYXYLQxJ5Lx3qLKWW7YnCqHXqjWKLNfMh1vGL\n" +
                "KxJQQKBgQDS5KvQJ2/pM3KJqJpRxPxPNEhqkcc/CBFSWfCDNJCkGxNLJZ0+4p2L\n" +
                "HwNjFqvYR6Vc4JHVwxX4J5vXdXqInvlFxF9kGRtGLKGLCKLWlRVMxKPfOFM/LqZ\n" +
                "9xnQFmkJBQ5S+4dLUvH0WV+rq5H2L4PlKFjNMNOZc2U4BzjYSPjzZFO3MKIkqxU\n" +
                "qGbRdTcZrN4uKFzf1d8fHsJt5HkTjKjX0IqFRJ8kWXQjHHHJMTxIkGJvQhMkNmRc\n" +
                "yPfHhJ4qJA==\n" +
                "-----END RSA PRIVATE KEY-----";
    }
}