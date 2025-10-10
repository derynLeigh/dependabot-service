package com.dependabot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for service monitoring
 * Provides basic health status without requiring authentication
 */
@RestController
public class HealthController {

    /**
     * Health check endpoint
     *
     * @return Map containing service status and name
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "dependabot-pr-service");

        return ResponseEntity.ok(healthResponse);
    }
}