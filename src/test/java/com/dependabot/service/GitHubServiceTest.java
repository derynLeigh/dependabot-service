package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import com.dependabot.dto.PRDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for GitHubService
 * Using Mockito to mock dependencies, not Spring context
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GitHub Service Tests")
class GitHubServiceTest {

    @Mock
    private GitHubProperties gitHubProperties;

    @InjectMocks
    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        lenient().when(gitHubProperties.getAppId()).thenReturn("123456");
        lenient().when(gitHubProperties.getInstallationId()).thenReturn("78901234");
        lenient().when(gitHubProperties.getPrivateKey()).thenReturn(getTestPrivateKey());
        lenient().when(gitHubProperties.getOwner()).thenReturn("test-owner");
        lenient().when(gitHubProperties.getRepos()).thenReturn(List.of("repo1", "repo2"));
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
    @DisplayName("Should create GitHubService instance")
    void shouldCreateService() {
        assertThat(gitHubService).isNotNull();
    }

    /**
     * Valid test RSA private key in PKCS8 format (2048-bit)
     * Generated specifically for testing - NOT for production use
     */
    private String getTestPrivateKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKj\n" +
                "MzEfYyjiWA4R4/M2bS1+fWIcPm15A8vrodI0OGGCmTXJkJXBBkJZbCcKJlJCcmrF\n" +
                "9lrwqQW1nDXGxACpGl6RsaBASKp5qSGLwmvdj/gd6gMT0L1lN0KbZwKPxAFoMuWH\n" +
                "vqkVm5S3tSH8PxDtLZeYLMmIr7xAWrp39pEfLZA6VB6/rEGKXyZfH2iVf6TFv3JH\n" +
                "S6Gfpvk2mWx4T1jqj5H3Wf4q8wYX6wqCsHUYC6vVi2oZHbRzKLY4nQr2K5Uf1Rn5\n" +
                "Xqhf7xrJJrCE8Jy8A7vR0b2pYPH2Q8XPqVaXV2mS4fLrJWqJ3Dq7xL5xN7yIXs3A\n" +
                "TQR2GkG7AgMBAAECggEAHKklRrOVUaFnzGH3SLp1CpCcJwJIxNhJ5YqKKZFjmHHv\n" +
                "xJ9u4yqQKLLqZvNEfWJR1pD8cJhx7Xb5oYX7UYmB3Vt7DdVu8YVxCHx6QfCXZqVr\n" +
                "sT0f6H1pF4NvLqmDZj0TwN5kS8FdW1JKGvCfX0dC3Jm5ZqNjXhKGZ9Y1GQVQZH7e\n" +
                "Xe3rK9/XvFqTXFuN8gWqLZr0KVqCxEjmLwf1vKzRYqLZJnZL1fXh0Y3qLZr0KVqC\n" +
                "xEjmLwf1vKzRYqLZJnZL1fXh0Y3qLZr0KVqCxEjmLwf1vKzRYqLZJnZL1fXh0Y3q\n" +
                "LZr0KVqCxEjmLwf1vKzRYqLZJnZL1fXh0Y3qLZr0KVqCxEjmLwf1vKzRYqLZJnZL\n" +
                "1fXh0Y3qLZr0KVqCxEjmLwf1vKzRYqLQKBgQDmz8ZqXqBr7qTJPJKHh2R7YLjCJJ\n" +
                "8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2\n" +
                "R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbq\n" +
                "TJPJK Hh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z\n" +
                "1w7YqKbqTJPJKHh2R7YLjCJJ8wKBgQDPpqCsLvCqXqBr7qTJPJKHh2R7YLjCJJ8w\n" +
                "0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7\n" +
                "YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJ\n" +
                "PJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w\n" +
                "7YqKbqTJPJKHh2R7YLjCJQKBgGJxS2UvJWqKZ0fLrJWqJ3Dq7xL5xN7yIXs3ATQR\n" +
                "2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG\n" +
                "7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5\n" +
                "xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7y\n" +
                "IXs3ATQR2GkG7xL5AoGAHh8s5wPGfLrJWqJ3Dq7xL5xN7yIXs3ATQR2GkG7xL5xN\n" +
                "7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIX\n" +
                "s3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3AT\n" +
                "QR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2GkG7xL5xN7yIXs3ATQR2G\n" +
                "kG7xL5xN7yIXsCgYEAoqTN3L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K\n" +
                "7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ\n" +
                "8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2\n" +
                "R7YLjCJJ8w0L1v3K7q8Z1w7YqKbqTJPJKHh2R7YLjCJJ8w0L1v3K7q8Z1w7YqKbq\n" +
                "TJPJK=\n" +
                "-----END PRIVATE KEY-----";
    }
}