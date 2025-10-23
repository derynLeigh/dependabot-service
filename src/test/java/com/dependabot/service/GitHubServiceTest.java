package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHub Service Tests")
class GitHubServiceTest {

    /**
     * Test RSA private key loaded from test resources
     * This is a dedicated test key that is never used in production
     */
    private static String loadTestPrivateKey() {
        try {
            return Files.readString(
                    Paths.get("src/test/resources/test-github-key.pem")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test private key", e);
        }
    }

    @Mock
    private GitHubProperties gitHubProperties;

    private GitHubService gitHubService;

    @BeforeEach
    void setUp() throws IOException {
        String testKey = loadTestPrivateKey();

        // Use lenient() to avoid "unnecessary stubbing" warnings for tests that override these
        lenient().when(gitHubProperties.getPrivateKeyContent()).thenReturn(testKey);
        lenient().when(gitHubProperties.getAppId()).thenReturn("123456");
        lenient().when(gitHubProperties.getInstallationId()).thenReturn("789012");
        lenient().when(gitHubProperties.getOwner()).thenReturn("test-owner");

        gitHubService = new GitHubService(gitHubProperties);
    }
    @Test
    @DisplayName("Should generate valid JWT token structure")
    void shouldGenerateValidJWTStructure() {
        String jwt = gitHubService.generateJWT();

        assertThat(jwt)
                .isNotNull()
                .contains(".");

        String[] parts = jwt.split("\\.");
        assertThat(parts)
                .as("JWT should have 3 parts: header, payload, signature")
                .hasSize(3);

        // Verify each part is base64-encoded
        assertThatCode(() -> Base64.getUrlDecoder().decode(parts[0]))
                .as("Header should be valid base64")
                .doesNotThrowAnyException();

        assertThatCode(() -> Base64.getUrlDecoder().decode(parts[1]))
                .as("Payload should be valid base64")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should use correct issuer from app ID")
    void shouldUseCorrectIssuer() {
        String testAppId = "test-app-123";
        when(gitHubProperties.getAppId()).thenReturn(testAppId);

        String jwt = gitHubService.generateJWT();

        assertThat(jwt).isNotNull();

        // Decode and verify issuer
        String[] parts = jwt.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        assertThat(payload)
                .as("JWT payload should contain correct issuer")
                .contains("\"iss\":\"" + testAppId + "\"");
    }

    @Test
    @DisplayName("Should handle JWT generation with valid key format")
    void shouldHandleJWTGenerationWithValidKey() {
        assertThatCode(() -> gitHubService.generateJWT())
                .as("JWT generation should succeed with valid key")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when private key is not configured")
    void shouldFailWhenPrivateKeyNotConfigured() throws IOException {
        when(gitHubProperties.getPrivateKeyContent())
                .thenThrow(new IllegalStateException("Either 'github.private-key' or 'github.private-key-file' must be configured"));

        assertThatThrownBy(() -> gitHubService.generateJWT())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate JWT")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should throw exception for invalid key format")
    void shouldFailWithInvalidKeyFormat() throws IOException {
        when(gitHubProperties.getPrivateKeyContent()).thenReturn("invalid-key-content");

        assertThatThrownBy(() -> gitHubService.generateJWT())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate JWT");
    }

    @Test
    @DisplayName("Should generate JWT with expiration time")
    void shouldGenerateJWTWithExpiration() {
        String jwt = gitHubService.generateJWT();

        String[] parts = jwt.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertThat(payload)
                .as("JWT should contain expiration claim")
                .contains("\"exp\":");
    }

    @Test
    @DisplayName("Should generate JWT with issued at time")
    void shouldGenerateJWTWithIssuedAt() {
        String jwt = gitHubService.generateJWT();

        String[] parts = jwt.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertThat(payload)
                .as("JWT should contain issued at claim")
                .contains("\"iat\":");
    }

    @Test
    @DisplayName("Should use RS256 algorithm for signing")
    void shouldUseRS256Algorithm() {
        String jwt = gitHubService.generateJWT();

        String[] parts = jwt.split("\\.");
        String header = new String(Base64.getUrlDecoder().decode(parts[0]));

        assertThat(header)
                .as("JWT header should specify RS256 algorithm")
                .contains("\"alg\":\"RS256\"");
    }
}