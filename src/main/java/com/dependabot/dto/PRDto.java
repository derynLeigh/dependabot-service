package com.dependabot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for Pull Request information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dependabot pull request information")
public class PRDto {

    @Schema(description = "GitHub PR number", example = "123")
    private Integer number;

    @Schema(description = "GitHub PR ID (globally unique)", example = "987654321")
    private Long id;

    @Schema(description = "PR title", example = "Bump spring-boot from 3.1.0 to 3.2.1")
    private String title;

    @Schema(description = "PR author username", example = "dependabot[bot]")
    private String author;

    @Schema(description = "Repository name", example = "techronymsService")
    private String repository;

    @Schema(description = "Full URL to the PR", example = "https://github.com/owner/repo/pull/123")
    private String url;

    @Schema(description = "PR state", example = "OPEN", allowableValues = {"OPEN", "CLOSED", "MERGED"})
    private String state;

    @Schema(description = "When the PR was created", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "When the PR was last updated", example = "2024-01-16T14:20:00Z")
    private Instant updatedAt;

    @Schema(description = "Name of the dependency being updated", example = "spring-boot")
    private String dependency;

    @Schema(description = "Current version of the dependency", example = "3.1.0")
    private String currentVersion;

    @Schema(description = "Proposed new version", example = "3.2.1")
    private String proposedVersion;

    @Schema(description = "PR description/body")
    private String body;

    @Schema(description = "Number of commits in the PR", example = "1")
    private Integer commits;

    @Schema(description = "Number of files changed", example = "2")
    private Integer filesChanged;

    @Schema(description = "Whether the PR has merge conflicts", example = "false")
    private Boolean hasConflicts;

    /**
     * Backward compatibility: map 'repo' to 'repository'
     */
    @Schema(hidden = true)
    public String getRepo() {
        return repository;
    }

    @Schema(hidden = true)
    public void setRepo(String repo) {
        this.repository = repo;
    }
}