package com.dependabot.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {

    /**
     * GitHub App ID
     * Required for authentication
     */
    @NotBlank(message = "GitHub App ID must not be blank")
    private String appId;

    /**
     * GitHub Installation ID
     * Identifies the installation of the GitHub App
     */
    @NotBlank(message = "GitHub Installation ID must not be blank")
    private String installationId;

    /**
     * GitHub App Private Key (direct content)
     * Used for generating JWT tokens
     * Either this OR privateKeyFile must be provided
     */
    private String privateKey;

    /**
     * Path to GitHub App Private Key file
     * Alternative to providing the key directly
     * Either this OR privateKey must be provided
     */
    private String privateKeyFile;

    /**
     * GitHub repository owner (username or organization)
     */
    @NotBlank(message = "GitHub Owner must not be blank")
    private String owner;

    /**
     * List of repository names to monitor
     */
    private List<String> repos;

    /**
     * Helper method to get the actual private key content
     * Reads from file if privateKeyFile is set, otherwise returns privateKey
     *
     * @return The private key content as a string
     * @throws IOException if the file cannot be read
     * @throws IllegalStateException if neither privateKey nor privateKeyFile is configured
     */
    public String getPrivateKeyContent() throws IOException {
        if (privateKey != null && !privateKey.trim().isEmpty()) {
            return privateKey;
        }

        if (privateKeyFile != null && !privateKeyFile.trim().isEmpty()) {
            return Files.readString(Paths.get(privateKeyFile));
        }

        throw new IllegalStateException(
                "Either 'github.private-key' or 'github.private-key-file' must be configured"
        );
    }
}