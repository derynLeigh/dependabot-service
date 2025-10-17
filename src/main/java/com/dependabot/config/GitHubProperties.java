package com.dependabot.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for GitHub App integration
 * Binds properties with prefix "github" from application.yml or environment variables
 */
@Setter
@Getter
@Component
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
     * GitHub App Private Key
     * Used for generating JWT tokens
     */
    @NotBlank(message = "GitHub Private Key must not be blank")
    private String privateKey;

    /**
     * GitHub repository owner (username or organization)
     */
    @NotBlank(message = "GitHub Owner must not be blank")
    private String owner;

    /**
     * List of repository names to monitor
     */
    private List<String> repos;

    // Getters and Setters

}