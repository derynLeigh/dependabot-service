package com.dependabot.controller;

import com.dependabot.dto.PRDto;
import com.dependabot.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for Dependabot pull requests
 * Provides endpoints to retrieve PR information
 */
@RestController
@RequestMapping("/api/prs")
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Pull Requests", description = "Endpoints for managing Dependabot pull requests")
public class PRController {

    private static final Logger log = LoggerFactory.getLogger(PRController.class);

    private final GitHubService gitHubService;

    public PRController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * Get all Dependabot PRs from configured repositories
     */
    @Operation(
            summary = "Get all Dependabot pull requests",
            description = "Retrieves all open Dependabot pull requests from all configured repositories"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all pull requests",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PRDto.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<PRDto>> getAllPRs() {
        log.debug("GET /api/prs - Fetching all Dependabot PRs");

        List<PRDto> prs = gitHubService.getAllDependabotPRs();

        log.debug("Returning {} PRs", prs.size());
        return ResponseEntity.ok(prs);
    }

    /**
     * Get Dependabot PRs for a specific repository
     */
    @Operation(
            summary = "Get pull requests for a specific repository",
            description = "Retrieves all open Dependabot pull requests for the specified repository"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved pull requests for the repository",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PRDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Repository not found",
                    content = @Content
            )
    })
    @GetMapping("/{repository}")
    public ResponseEntity<List<PRDto>> getPRsByRepository(
            @Parameter(description = "Repository name", example = "techronymsService")
            @PathVariable String repository) {
        log.debug("GET /api/prs/{} - Fetching PRs for repository", repository);

        List<PRDto> prs = gitHubService.getDependabotPRs(repository);

        log.debug("Returning {} PRs for repository: {}", prs.size(), repository);
        return ResponseEntity.ok(prs);
    }
}