package com.dependabot;

import com.dependabot.config.ApiInfoProperties;
import com.dependabot.config.GitHubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({
        GitHubProperties.class,
        ApiInfoProperties.class
})
public class DependabotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DependabotApplication.class, args);
    }
}