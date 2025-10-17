package com.dependabot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRDto {

    /**
     * GitHub PR number (unique within repository)
     */
    private Integer number;

    /**
     * GitHub PR ID (globally unique)
     */
    private Long id;

    /**
     * PR title
     */
    private String title;

    /**
     * PR author/creator username
     */
    private String author;

    /**
     * Repository name
     */
    private String repository;

    /**
     * Full URL to the PR on GitHub
     */
    private String url;

    /**
     * PR state (OPEN, CLOSED, MERGED)
     */
    private String state;

    /**
     * When the PR was created
     */
    private Instant createdAt;

    /**
     * When the PR was last updated
     */
    private Instant updatedAt;

    /**
     * Dependency name being updated
     * Example: "spring-boot", "lodash"
     */
    private String dependency;

    /**
     * Current version of the dependency
     */
    private String currentVersion;

    /**
     * Proposed/new version of the dependency
     */
    private String proposedVersion;

    /**
     * PR body/description
     */
    private String body;

    /**
     * Number of commits in the PR
     */
    private Integer commits;

    /**
     * Number of files changed
     */
    private Integer filesChanged;

    /**
     * Whether the PR has conflicts
     */
    private Boolean hasConflicts;

    /**
     * Backward compatibility: map 'repo' to 'repository'
     */
    public String getRepo() {
        return repository;
    }

    public void setRepo(String repo) {
        this.repository = repo;
    }
}