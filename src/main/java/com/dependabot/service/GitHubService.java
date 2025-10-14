package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import com.dependabot.dto.PRDto;
import io.jsonwebtoken.Jwts;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for integrating with GitHub API
 * Handles authentication and fetching Dependabot pull requests
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private final GitHubProperties gitHubProperties;

    public GitHubService(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
    }

    /**
     * Generate JWT token for GitHub App authentication
     * JWT is valid for 10 minutes as per GitHub's requirements
     *
     * @return JWT token string
     */
    public String generateJWT() {
        try {
            log.debug("Generating JWT token for GitHub App authentication");

            // Remove PEM headers and footers, and whitespace
            String privateKeyContent = gitHubProperties.getPrivateKey()
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Decode the private key
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            // Generate JWT using new JJWT API
            long nowMillis = System.currentTimeMillis();
            long expMillis = nowMillis + 600000; // 10 minutes

            String jwt = Jwts.builder()
                    .issuedAt(new Date(nowMillis))
                    .expiration(new Date(expMillis))
                    .issuer(gitHubProperties.getAppId())
                    .signWith(privateKey)  // Algorithm is auto-detected from key
                    .compact();

            log.debug("JWT token generated successfully");
            return jwt;

        } catch (Exception e) {
            log.error("Failed to generate JWT for GitHub App", e);
            throw new RuntimeException("Failed to generate JWT for GitHub App", e);
        }
    }

    /**
     * Get installation access token using JWT
     *
     * @return Installation access token
     */
    public String getInstallationToken() {
        try {
            log.debug("Fetching installation token for installation ID: {}",
                    gitHubProperties.getInstallationId());

            String jwt = generateJWT();
            GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwt).build();

            GHAppInstallation installation = gitHubApp
                    .getApp()
                    .getInstallationById(Long.parseLong(gitHubProperties.getInstallationId()));

            GHAppInstallationToken token = installation.createToken().create();

            log.debug("Installation token retrieved successfully");
            return token.getToken();

        } catch (IOException e) {
            log.error("Failed to get installation token from GitHub for installation ID: {}",
                    gitHubProperties.getInstallationId(), e);
            throw new RuntimeException("Failed to get installation token from GitHub", e);
        }
    }

    /**
     * Fetch Dependabot PRs from a single repository
     *
     * @param owner Repository owner
     * @param repoName Repository name
     * @return List of Dependabot PRs as DTOs
     * @throws IOException if GitHub API call fails
     */
    public List<PRDto> fetchDependabotPRs(String owner, String repoName) throws IOException {
        log.debug("Fetching Dependabot PRs from repository: {}/{}", owner, repoName);

        String token = getInstallationToken();
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();

        GHRepository repository = github.getRepository(owner + "/" + repoName);
        List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);

        List<PRDto> dependabotPRs = pullRequests.stream()
                .filter(this::isDependabotPR)
                .map(this::toPRDto)
                .collect(Collectors.toList());

        log.debug("Found {} Dependabot PRs in {}/{}", dependabotPRs.size(), owner, repoName);
        return dependabotPRs;
    }

    /**
     * Check if a PR is from Dependabot
     *
     * @param pr GitHub pull request
     * @return true if PR is from dependabot[bot]
     */
    private boolean isDependabotPR(GHPullRequest pr) {
        try {
            GHUser user = pr.getUser();
            return user != null && "dependabot[bot]".equals(user.getLogin());
        } catch (IOException e) {
            log.warn("Failed to get user for PR {}", pr.getId(), e);
            return false;
        }
    }

    /**
     * Convert GitHub PR to DTO format
     *
     * @param pr GitHub pull request
     * @return PRDto
     */
    public PRDto toPRDto(GHPullRequest pr) {
        try {
            PRDto dto = new PRDto();
            dto.setId((long) pr.getNumber());
            dto.setTitle(pr.getTitle());
            dto.setUrl(pr.getHtmlUrl().toString());
            dto.setRepo(pr.getRepository().getName());
            dto.setCreatedAt(pr.getCreatedAt().toInstant());
            dto.setUpdatedAt(pr.getUpdatedAt().toInstant());
            return dto;
        } catch (IOException e) {
            log.error("Failed to convert PR {} to DTO", pr.getId(), e);
            throw new RuntimeException("Failed to convert PR to DTO", e);
        }
    }
}