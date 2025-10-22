package com.dependabot.controller;

import com.dependabot.dto.PRDto;
import com.dependabot.service.GitHubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PRController.class)
@DisplayName("PR Controller Tests")
class PRControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @Test
    @DisplayName("GET /api/prs should return 200 OK")
    void getAllPRs_shouldReturn200() throws Exception {
        when(gitHubService.getAllDependabotPRs()).thenReturn(List.of());

        mockMvc.perform(get("/api/prs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prs should return JSON")
    void getAllPRs_shouldReturnJson() throws Exception {
        when(gitHubService.getAllDependabotPRs()).thenReturn(List.of());

        mockMvc.perform(get("/api/prs"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/prs should return list of PRs")
    void getAllPRs_shouldReturnPRList() throws Exception {
        PRDto pr = PRDto.builder()
                .number(1)
                .title("Test PR")
                .repository("test-repo")
                .author("dependabot[bot]")
                .url("https://github.com/test/test/pull/1")
                .createdAt(Instant.now())
                .build();

        when(gitHubService.getAllDependabotPRs()).thenReturn(List.of(pr));

        mockMvc.perform(get("/api/prs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].number").value(1))
                .andExpect(jsonPath("$[0].title").value("Test PR"));
    }

    @Test
    @DisplayName("GET /api/prs/{repository} should return 200 OK")
    void getPRsByRepository_shouldReturn200() throws Exception {
        when(gitHubService.getDependabotPRs(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/prs/test-repo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prs/{repository} should return PRs for repository")
    void getPRsByRepository_shouldReturnRepositoryPRs() throws Exception {
        PRDto pr = PRDto.builder()
                .number(1)
                .title("Test PR")
                .repository("test-repo")
                .author("dependabot[bot]")
                .url("https://github.com/test/test/pull/1")
                .createdAt(Instant.now())
                .build();

        when(gitHubService.getDependabotPRs("test-repo")).thenReturn(List.of(pr));

        mockMvc.perform(get("/api/prs/test-repo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].repository").value("test-repo"));
    }

    @Test
    @DisplayName("GET /api/prs/{repository} should return empty list for repo with no PRs")
    void getPRsByRepository_shouldReturnEmptyListForNoPRs() throws Exception {
        when(gitHubService.getDependabotPRs("empty-repo")).thenReturn(List.of());

        mockMvc.perform(get("/api/prs/empty-repo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}