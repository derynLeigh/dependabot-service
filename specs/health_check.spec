# Health Check API

The health check endpoint provides information about the service status.

## Service should respond to health check requests
* Start the application on a random port
* Make a GET request to "/health"
* Response status code should be "200"
* Response content type should be "application/json"

## Health check should return correct service information
* Start the application on a random port
* Make a GET request to "/health"
* Response should contain field "status" with value "UP"
* Response should contain field "service" with value "dependabot-pr-service"

## Health check should work without authentication
* Start the application on a random port
* Make a GET request to "/health" without authentication
* Response status code should be "200"