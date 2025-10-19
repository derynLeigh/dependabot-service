package com.dependabot.service;

import com.dependabot.config.GitHubProperties;
import com.dependabot.dto.PRDto;
import io.jsonwebtoken.Jwts;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with GitHub API
 * Handles authentication and retrieval of Dependabot pull requests
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private static final String DEPENDABOT_LOGIN = "dependabot[bot]";
    private static final String DEPENDABOT_APP = "dependabot";

    private final GitHubProperties gitHubProperties;

    public GitHubService(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
    }

    /**
     * Generate JWT token for GitHub App authentication
     * Uses modern JJWT API (non-deprecated methods)
     *
     * @return JWT token string
     */

    public String generateJWT() {
        try {
            String keyContent = gitHubProperties.getPrivateKey();

            // Determine key format and parse accordingly
            PrivateKey privateKey;
            if (keyContent.contains("BEGIN RSA PRIVATE KEY")) {
                // PKCS#1 format - need to convert to PKCS#8
                privateKey = parsePKCS1PrivateKey(keyContent);
            } else {
                // PKCS#8 format
                privateKey = parsePKCS8PrivateKey(keyContent);
            }

            // Create JWT using modern API
            Instant now = Instant.now();
            Instant expiration = now.plus(10, ChronoUnit.MINUTES);

            return Jwts.builder()
                    .issuer(gitHubProperties.getAppId())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();

        } catch (Exception e) {
            log.error("Failed to generate JWT", e);
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    /**
     * Parse PKCS#8 format private key
     */
    private PrivateKey parsePKCS8PrivateKey(String keyContent) throws Exception {
        String privateKeyPEM = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Parse PKCS#1 format private key (GitHub App default format)
     * Converts PKCS#1 to PKCS#8 format
     */
    private PrivateKey parsePKCS1PrivateKey(String keyContent) throws Exception {
        String privateKeyPEM = keyContent
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] pkcs1Bytes = Base64.getDecoder().decode(privateKeyPEM);

        // Convert PKCS#1 to PKCS#8
        byte[] pkcs8Bytes = convertPKCS1ToPKCS8(pkcs1Bytes);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Convert PKCS#1 format to PKCS#8 format
     */
    private byte[] convertPKCS1ToPKCS8(byte[] pkcs1Bytes) {
        // PKCS#8 header for RSA private key
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[] {
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // SEQUENCE
                0x2, 0x1, 0x0, // INTEGER 0 (version)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // RSA OID
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // OCTET STRING
        };

        byte[] pkcs8Bytes = new byte[pkcs8Header.length + pkcs1Bytes.length];
        System.arraycopy(pkcs8Header, 0, pkcs8Bytes, 0, pkcs8Header.length);
        System.arraycopy(pkcs1Bytes, 0, pkcs8Bytes, pkcs8Header.length, pkcs1Bytes.length);

        return pkcs8Bytes;
    }

    /**
     * Get authenticated GitHub client
     *
     * @return GitHub client
     * @throws IOException if authentication fails
     */
    private GitHub getGitHubClient() throws IOException {
        String jwt = generateJWT();

        // Create GitHub client with JWT
        GitHub gitHubApp = new GitHubBuilder()
                .withJwtToken(jwt)
                .build();

        // Get installation token
        GHAppInstallation installation = gitHubApp.getApp()
                .getInstallationById(Long.parseLong(gitHubProperties.getInstallationId()));

        GHAppInstallationToken token = installation.createToken().create();

        // Return authenticated client
        return new GitHubBuilder()
                .withAppInstallationToken(token.getToken())
                .build();
    }

    /**
     * Get Dependabot pull requests for a specific repository
     * Results are cached to reduce API calls
     *
     * @param repositoryName name of the repository
     * @return list of Dependabot PRs as DTOs
     */
    @Cacheable(value = "github-prs", key = "#repositoryName")
    public List<PRDto> getDependabotPRs(String repositoryName) {
        try {
            GitHub github = getGitHubClient();
            String owner = gitHubProperties.getOwner();

            log.debug("Fetching Dependabot PRs for {}/{}", owner, repositoryName);

            GHRepository repository = github.getRepository(owner + "/" + repositoryName);
            List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);

            return pullRequests.stream()
                    .filter(this::isDependabotPR)
                    .map(pr -> convertToPRDto(pr, repositoryName))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error fetching PRs for repository: {}", repositoryName, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get Dependabot pull requests from multiple repositories
     *
     * @param repositories list of repository names
     * @return combined list of Dependabot PRs from all repositories
     */
    public List<PRDto> getAllDependabotPRs(List<String> repositories) {
        return repositories.stream()
                .flatMap(repo -> getDependabotPRs(repo).stream())
                .collect(Collectors.toList());
    }

    /**
     * Get all Dependabot PRs from configured repositories
     *
     * @return list of all Dependabot PRs
     */
    public List<PRDto> getAllDependabotPRs() {
        return getAllDependabotPRs(gitHubProperties.getRepos());
    }

    /**
     * Check if a pull request is from Dependabot
     *
     * @param pr GitHub pull request
     * @return true if PR is from Dependabot
     */
    private boolean isDependabotPR(GHPullRequest pr) {
        try {
            GHUser author = pr.getUser();
            String login = author.getLogin();

            return DEPENDABOT_LOGIN.equalsIgnoreCase(login) ||
                    login.toLowerCase().contains(DEPENDABOT_APP);

        } catch (IOException e) {
            log.warn("Error checking PR author", e);
            return false;
        }
    }

    /**
     * Convert GitHub PR to DTO
     *
     * @param pr GitHub pull request
     * @param repositoryName repository name
     * @return PR DTO
     */
    private PRDto convertToPRDto(GHPullRequest pr, String repositoryName) {
        try {
            return PRDto.builder()
                    .number(pr.getNumber())
                    .id(pr.getId())
                    .title(pr.getTitle())
                    .author(pr.getUser().getLogin())
                    .repository(repositoryName)
                    .url(pr.getHtmlUrl().toString())
                    .state(pr.getState().name())
                    .createdAt(pr.getCreatedAt().toInstant())
                    .updatedAt(pr.getUpdatedAt().toInstant())
                    .body(pr.getBody())
                    .commits(pr.getCommits())
                    .filesChanged(pr.getChangedFiles())
                    .hasConflicts(pr.getMergeable() != null && !pr.getMergeable())
                    .dependency(extractDependency(pr.getTitle()))
                    .currentVersion(extractCurrentVersion(pr.getTitle()))
                    .proposedVersion(extractProposedVersion(pr.getTitle()))
                    .build();

        } catch (IOException e) {
            log.error("Error converting PR to DTO", e);
            throw new RuntimeException("Error converting PR", e);
        }
    }

    /**
     * Extract dependency name from PR title
     * Example: "Bump spring-boot from 3.1.0 to 3.2.1" -> "spring-boot"
     *
     * @param title PR title
     * @return dependency name or null
     */
    private String extractDependency(String title) {
        try {
            if (title.toLowerCase().startsWith("bump ")) {
                String withoutBump = title.substring(5); // Remove "Bump "
                int fromIndex = withoutBump.toLowerCase().indexOf(" from ");
                if (fromIndex > 0) {
                    return withoutBump.substring(0, fromIndex).trim();
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract dependency from title: {}", title);
        }
        return null;
    }

    /**
     * Extract current version from PR title
     * Example: "Bump spring-boot from 3.1.0 to 3.2.1" -> "3.1.0"
     *
     * @param title PR title
     * @return current version or null
     */
    private String extractCurrentVersion(String title) {
        try {
            int fromIndex = title.toLowerCase().indexOf(" from ");
            int toIndex = title.toLowerCase().indexOf(" to ");
            if (fromIndex > 0 && toIndex > fromIndex) {
                return title.substring(fromIndex + 6, toIndex).trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract current version from title: {}", title);
        }
        return null;
    }

    /**
     * Extract proposed version from PR title
     * Example: "Bump spring-boot from 3.1.0 to 3.2.1" -> "3.2.1"
     *
     * @param title PR title
     * @return proposed version or null
     */
    private String extractProposedVersion(String title) {
        try {
            int toIndex = title.toLowerCase().indexOf(" to ");
            if (toIndex > 0) {
                String afterTo = title.substring(toIndex + 4).trim();
                // Remove any trailing text in parentheses or brackets
                int endIndex = afterTo.length();
                for (char c : new char[]{'(', '[', ' ', '\n'}) {
                    int idx = afterTo.indexOf(c);
                    if (idx > 0 && idx < endIndex) {
                        endIndex = idx;
                    }
                }
                return afterTo.substring(0, endIndex).trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract proposed version from title: {}", title);
        }
        return null;
    }
}