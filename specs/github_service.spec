# GitHub Service Integration

The GitHub Service should authenticate with GitHub and retrieve Dependabot pull requests.

## Service should authenticate with GitHub
* Start the application with GitHub configuration
* GitHub service should be initialized
* GitHub service should generate valid JWT token
* JWT token should have three parts

## Service should retrieve pull requests from a repository
* Start the application with GitHub configuration
* Get pull requests for repository "test-repo"
* Pull requests list should not be null
* Pull requests should be returned as DTOs

## Service should filter Dependabot pull requests
* Start the application with GitHub configuration
* Get pull requests for repository "test-repo"
* Filter only Dependabot pull requests
* All returned PRs should be from Dependabot

## Service should handle repository with no pull requests
* Start the application with GitHub configuration
* Get pull requests for repository "empty-repo"
* Pull requests list should be empty

## Service should handle non-existent repository
* Start the application with GitHub configuration
* Get pull requests for repository "non-existent-repo"
* Should handle repository not found error gracefully

## Service should retrieve PRs from multiple repositories
* Start the application with GitHub configuration
* Get pull requests for multiple repositories "repo1,repo2,repo3"
* Pull requests should contain results from all repositories
* Each PR should have repository name populated