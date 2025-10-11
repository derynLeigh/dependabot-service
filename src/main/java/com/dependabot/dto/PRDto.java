package com.dependabot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class PRDto {

    // Getters and Setters
    private Long id;
    private String title;
    private String url;
    private String repo;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructors
    public PRDto() {
    }

    public PRDto(Long id, String title, String url, String repo, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.repo = repo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}