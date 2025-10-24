package com.dependabot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the scheduled PR refresh task
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    /**
     * Whether the scheduler is enabled
     * Default: false (disabled)
     */
    private boolean enabled = false;

    /**
     * Cron expression for schedule
     * Default: 7 AM daily (0 7 * * *)
     */
    @NotBlank(message = "Cron schedule must not be blank")
    private String cron = "0 7 * * *";

    /**
     * Maximum number of retry attempts on failure
     * Default: 3
     */
    @Min(value = 0, message = "Max retries must be non-negative")
    private int maxRetries = 3;

    /**
     * Delay between retries in milliseconds
     * Default: 5000ms (5 seconds)
     */
    @Min(value = 0, message = "Retry delay must be non-negative")
    private long retryDelayMs = 5000;
}