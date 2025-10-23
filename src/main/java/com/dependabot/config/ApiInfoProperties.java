package com.dependabot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Setter
@Getter
@ConfigurationProperties(prefix = "api.info")
public class ApiInfoProperties {

    private String title = "Dependabot PR Service";
    private String description = "REST API for managing and summarizing Dependabot pull requests";
    private String version = "1.0.0";

    @NestedConfigurationProperty
    private Contact contact = new Contact();

    @Setter
    @Getter
    public static class Contact {
        private String name = "Dependabot PR Service";
        private String email = "support@example.com";
        private String url;
    }
}