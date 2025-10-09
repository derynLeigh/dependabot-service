package com.dependabot.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@DisplayName("Health Controller Tests")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /health should return 200 OK")
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/health"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /health should return JSON content type")
    void health_shouldReturnJson() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /health should return status field with value UP")
    void health_shouldReturnStatusUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /health should return service field with correct name")
    void health_shouldReturnServiceName() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(jsonPath("$.service").value("dependabot-pr-service"));
    }

    @Test
    @DisplayName("GET /health should return both status and service fields")
    void health_shouldReturnCompleteResponse() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("dependabot-pr-service"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.service").exists());
    }
}