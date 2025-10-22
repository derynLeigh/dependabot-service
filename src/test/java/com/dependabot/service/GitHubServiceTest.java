package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GitHubService
 * Tests JWT generation, authentication, and PR retrieval logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GitHub Service Tests")
class GitHubServiceTest {

    /**
     * Valid RSA private key for testing (2048-bit) - NOT A REAL SECRET
     * Generated specifically for unit testing purposes only
     * This key is intentionally committed for testing and should never be used in production
     */
    // gg-ignore-secret
    // nosec
    private static final String VALID_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDS51cqmddFpZkV
            WKT0m4bGZ23rBY3l0H0jUBa9z9Ral6Jd39NYE3pOpoBu9KsDDtmdMi97T/QeK5HA
            x6Cyy+FZh6CKaLuuymcBCJ6dYlKEq1HODxc5VCAm9JMlmxQd5hA+j/uE/1CjF5IH
            2Ljp+OIJiRVT7gD3r+uww3i9lodsKSLFSIo1AEl6ow1I7QAXXy5ADY2eSjA3Fk2/
            P6l+HW7lp85sPozZTmB8InNkL9QAAzhrDTNTCGEfnFCiEIDn27AQzZ95q3/VkuBD
            qaByUA0vxHsrRebpkJh3WvmTv9XRBAkqarbM2y1B9ASwhgP22RRCuoeXnLIqPDNq
            34sPcjjhAgMBAAECggEAZahf8idcRfRnwQvM2yDaSmkRaO7w8jzYDLzucUirpY0g
            lNot727YDfAgyRrDEYLHbweBLpGf7Cj5JPC0a2ptsW93+S85hCuIkc17UwZRGyTP
            FiFdAuOjadOK194TyMObweQ6CuXcEvjqCUE0BIdUQN9kOePncHxKAZZ+7PeRSBb9
            153SriEhZ+locxzsM+JCNtP1Cs/dwrF/yRMRC5uhJHIjvpjiagbxo0vnWN9+y3E4
            UVyuQantBIcJypi74A3NZ+hJukb8b09sCwCQMKozbM0e7v033I60rvprKDock0rg
            cyHRiocFTDFXNEeds8EGTQNwxBoMrJRx8SXQvFKZFwKBgQDuqqe4ltPoj8aWh9C0
            lW0tjHZhLSdA0bvhNuclI0AN3HvYPA9IUyzs8r5fqqVqiZyYb5tIbRg5S7XDGXFu
            2vD3pBHDWv69M2SxyK/Aa9kPEsqV6AzZwsmvbwgPkgs3qZ9QZLYSl0vYoCu/cN+V
            mnCIzDxQN/MKNZNcot5KM15TpwKBgQDiOIKc6w+qfSa71hviFbN4x/6x0qPWC9a+
            LvXWk4fAJsDAu4sW6FC5c56KuaCFsnvHOI2LAmXRzPBYCtsXu6k9ocGnMDO0p+UU
            vBYi1y3ZgOfbuSy8DILy0xzKa5kIlHPuUbH+fRKg2AqOw0F9A5Q8oJMu2ygI+rM3
            R0V8TyfANwKBgH+b7XuL4ZfqL/NSgOvdLXf6L118CA3nVj5v3Z0EwP3oUqtmSYGR
            P2cdz15VXoadN4ZlvjA2bIpOCcxcMUNlewvdxMWgRK4i6YcJV7dZssAVew41+ZEB
            Tlcn1baFjsvfPNh+UL+V967GzT1Z+6IA5oSuNt1pXOCrjrQsPRCNcLTnAoGBALc3
            25lpzCNh/3gMfkLLHrOJD2BIYMdgiUk5UYS7sivPdzfh7VHdUFwTJ2nl0/vUxelh
            hUn75Cla2aIFENOU+wpeszBMmuQLQz9a4nbPXmQVwjO0M2YOoBQ5Jc1P/f7z9T0k
            z4t1ExxQPaTIwYaV2n6L8wj1GOTE31e7Eq0OkvxbAoGAJ/PEHAuCJlycwaXY6exZ
            2EnkKsU702Fqwg0Gn+LLEd47hZqhvBanz5PdRfBm9SKkAGLw85cDHl4ATTICzR6B
            ShZQKF6/LN+JbMLUwbvZ/yVH5QySHmqlGy+GJGW7nGbFrEKnYuylGzHZxHjKgq03
            PPMPWG3fW7JY5oIiAhn2Lp8=
            -----END PRIVATE KEY-----
            """;

    @Mock
    private GitHubProperties gitHubProperties;

    private GitHubService gitHubService;

    @BeforeEach
    void setUp() throws IOException {
        // Use lenient() to avoid "unnecessary stubbing" warnings for tests that override these
        lenient().when(gitHubProperties.getPrivateKeyContent()).thenReturn(VALID_PRIVATE_KEY);
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