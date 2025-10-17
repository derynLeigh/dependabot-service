# GitHub Service Integration

The GitHub Service should authenticate with GitHub and retrieve Dependabot pull requests.

## Service should authenticate with GitHub
* Start the application with GitHub configuration
* GitHub service should be initialized
* GitHub service should generate valid JWT token
* JWT token should have three parts

## Service should retrieve pull requests from a repository
* Start the application with GitHub configuration
* Get pull requests for repository "techronymsService"
* Pull requests list should not be null

## Service should handle repository with no pull requests
* Start the application with GitHub configuration
* Get pull requests for repository "non-existent-empty-repo"
* Pull requests list should be empty

## Service should handle non-existent repository gracefully
* Start the application with GitHub configuration
* Get pull requests for repository "definitely-does-not-exist-12345"
* Should handle repository not found error gracefully

## Service should handle multiple repositories
* Start the application with GitHub configuration
* Get pull requests for repositories "techronymsService,techronyms-user-service,dependabot-pr-summariser"
* Pull requests list should not be null

## Service should validate Dependabot PRs when they exist
tags: requires-prs
* Start the application with GitHub configuration
* Get pull requests for repository "techronymsService"
* If pull requests exist, all should be from Dependabot
* If pull requests exist, each should have required fields