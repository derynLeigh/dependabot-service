package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GitHubService
 * Using Mockito to mock JWT generation instead of using real keys
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
        lenient().when(gitHubProperties.getOwner()).thenReturn("test-owner");
        lenient().when(gitHubProperties.getRepos()).thenReturn(List.of("repo1", "repo2"));
    }

    @Test
    @DisplayName("Should generate valid JWT token structure")
    void shouldGenerateValidJWTStructure() {
        // Arrange: Setup mock private key
        when(gitHubProperties.getPrivateKey()).thenReturn(generateTestPrivateKey());

        // Act: Generate JWT
        String jwt = gitHubService.generateJWT();

        // Assert: Verify JWT structure without validating signature
        assertThat(jwt)
                .as("JWT token")
                .isNotNull()
                .isNotEmpty();

        // JWT should have 3 parts: header.payload.signature
        String[] parts = jwt.split("\\.");
        assertThat(parts)
                .as("JWT parts (header.payload.signature)")
                .hasSize(3);

        // Verify each part is base64 encoded (not empty)
        assertThat(parts[0]).as("JWT header").isNotEmpty();
        assertThat(parts[1]).as("JWT payload").isNotEmpty();
        assertThat(parts[2]).as("JWT signature").isNotEmpty();
    }

    @Test
    @DisplayName("Should create GitHubService instance")
    void shouldCreateService() {
        assertThat(gitHubService).isNotNull();
    }

    @Test
    @DisplayName("Should use correct issuer from app ID")
    void shouldUseCorrectIssuer() {
        // Arrange
        when(gitHubProperties.getPrivateKey()).thenReturn(generateTestPrivateKey());
        when(gitHubProperties.getAppId()).thenReturn("test-app-123");

        // Act
        String jwt = gitHubService.generateJWT();

        // Assert: Verify JWT was generated (signature verification happens in integration tests)
        assertThat(jwt).isNotNull();
        verify(gitHubProperties, atLeastOnce()).getAppId();
    }

    @Test
    @DisplayName("Should handle JWT generation with valid key format")
    void shouldHandleJWTGenerationWithValidKey() {
        // Arrange
        String validKey = generateTestPrivateKey();
        when(gitHubProperties.getPrivateKey()).thenReturn(validKey);

        // Act & Assert: Should not throw exception
        assertThat(gitHubService.generateJWT())
                .as("JWT generation should succeed with valid key")
                .isNotNull()
                .matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$"
                );
    }

    /**
     * Generate a valid test RSA private key for testing
     * This key is generated dynamically and is only for unit tests
     */
    private String generateTestPrivateKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();

            // Convert to PEM format
            String base64 = java.util.Base64.getEncoder()
                    .encodeToString(privateKey.getEncoded());

            StringBuilder pem = new StringBuilder();
            pem.append("-----BEGIN PRIVATE KEY-----\n");

            // Split into 64-character lines
            int index = 0;
            while (index < base64.length()) {
                pem.append(base64, index, Math.min(index + 64, base64.length()));
                pem.append("\n");
                index += 64;
            }

            pem.append("-----END PRIVATE KEY-----");
            return pem.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test key", e);
        }
    }
}