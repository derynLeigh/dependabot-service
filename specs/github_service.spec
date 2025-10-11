# GitHub Service Integration

The service should authenticate with GitHub and fetch Dependabot pull requests.

## Service should generate valid JWT token for GitHub App authentication
* Create GitHub service with valid configuration
* Generate JWT token
* JWT token should not be null
* JWT token should contain three parts separated by dots
* JWT token should be valid for 10 minutes

## Service should fetch Dependabot PRs from a single repository
* Create GitHub service with mock credentials
* Mock GitHub API to return sample pull requests
* Fetch pull requests from repository "test-repo"
* Should return list of pull requests
* All returned PRs should be from dependabot bot user

## Service should filter out non-Dependabot PRs
* Create GitHub service with mock credentials
* Mock GitHub API with mixed PR authors
* Fetch pull requests from repository "test-repo"
* Should only return Dependabot PRs
* Should not include PRs from regular users

## Service should convert GitHub PR to DTO format
* Create a sample GitHub pull request
* Convert pull request to DTO
* DTO should contain PR id
* DTO should contain PR title
* DTO should contain PR URL
* DTO should contain repository name
* DTO should contain created date
* DTO should contain updated date

## Service should handle repository fetch errors gracefully
* Create GitHub service with mock credentials
* Mock GitHub API to throw an error for specific repository
* Attempt to fetch from failing repository
* Should capture error details
* Error should include repository name
* Error should include error message