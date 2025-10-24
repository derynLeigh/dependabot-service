# Scheduled PR Refresh

The scheduler should periodically refresh Dependabot PR data from GitHub and update the cache.

## Scheduler should be disabled by default
* Scheduler should not be enabled by default
* No scheduled tasks should run when disabled

## Scheduler should run on configured schedule when enabled
* Enable scheduler with cron expression "*/5 * * * * *"
* Wait for scheduler to execute
* Scheduler should have executed at least once
* Cache should contain fresh PR data

## Scheduler should evict cache before refresh
* Enable scheduler
* Store initial cache data
* Wait for scheduler to execute
* Old cache data should be evicted
* New cache data should be present

## Scheduler should handle GitHub API errors gracefully
* Enable scheduler
* Simulate GitHub API failure
* Wait for scheduler to execute
* Scheduler should log error
* Scheduler should not crash application

## Scheduler should retry on failure
* Enable scheduler with max retries of 3
* Simulate temporary GitHub API failure
* Wait for scheduler to execute with retries
* Scheduler should have retried 3 times
* Scheduler should eventually succeed after retry

## Scheduler should refresh all configured repositories
* Enable scheduler
* Configure multiple repositories
* Wait for scheduler to execute
* All repositories should be refreshed
* Cache should contain PRs from all repositories

## Scheduler should log execution metrics
* Enable scheduler
* Wait for scheduler to execute
* Logs should contain execution start time
* Logs should contain execution duration
* Logs should contain number of PRs refreshed

## Scheduler execution should not block API requests
* Enable scheduler with long-running task
* Make API request while scheduler is running
* API request should complete successfully
* API should not be blocked by scheduler