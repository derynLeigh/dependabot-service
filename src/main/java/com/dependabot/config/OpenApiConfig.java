package com.dependabot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration
 * Configures API documentation accessible at /swagger-ui.html
 */
@Configuration
@EnableConfigurationProperties(ApiInfoProperties.class)
public class OpenApiConfig {

    private final ApiInfoProperties apiInfoProperties;

    public OpenApiConfig(ApiInfoProperties apiInfoProperties) {
        this.apiInfoProperties = apiInfoProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact()
                .name(apiInfoProperties.getContact().getName())
                .email(apiInfoProperties.getContact().getEmail());

        // Only set URL if provided
        if (apiInfoProperties.getContact().getUrl() != null &&
                !apiInfoProperties.getContact().getUrl().isEmpty()) {
            contact.url(apiInfoProperties.getContact().getUrl());
        }

        Info info = new Info()
                .title(apiInfoProperties.getTitle())
                .version(apiInfoProperties.getVersion())
                .description(apiInfoProperties.getDescription())
                .contact(contact);

        return new OpenAPI().info(info);
    }
}