# REST API

The service should expose pull request data through REST endpoints with OpenAPI documentation.

## Swagger UI should be accessible
* Start the REST API
* Make GET request to "/swagger-ui.html"
* Response status code should be "200"

## OpenAPI specification should be available
* Start the REST API
* Make GET request to "/api-docs"
* Response status code should be "200"
* Response should be valid JSON

## API should return all Dependabot PRs
* Start the REST API
* Make GET request to "/api/prs"
* Response status code should be "200"
* Response should be valid JSON
* Response should contain list of pull requests

## API should return PRs for specific repository
* Start the REST API
* Make GET request to "/api/prs/techronymsService"
* Response status code should be "200"
* Response should contain PRs for repository "techronymsService"

## API should handle repository with no PRs
* Start the REST API
* Make GET request to "/api/prs/empty-repo"
* Response status code should be "200"
* Response should contain empty list

## API should return 404 for invalid endpoints
* Start the REST API
* Make GET request to "/api/invalid-endpoint"
* Response status code should be "404"

## API should support CORS
* Start the REST API
* Make OPTIONS request to "/api/prs"
* Response should include CORS headers