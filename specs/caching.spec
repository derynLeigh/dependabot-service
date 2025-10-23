# Caching Layer

The service should cache GitHub API responses to improve performance and reduce API calls.

## Cache should store PR data
* Start the application with caching enabled
* Get pull requests for repository "techronymsService"
* Store the initial response time
* Get pull requests for repository "techronymsService" again
* Second request should be faster than first request

## Cache should have configurable TTL
* Start the application with caching enabled
* Get pull requests for repository "techronymsService"
* Pull requests should be cached
* Wait for cache to expire
* Get pull requests for repository "techronymsService" again
* Cache should have been refreshed

## Cache should be repository-specific
* Start the application with caching enabled
* Get pull requests for repository "techronymsService"
* Get pull requests for repository "techronyms-user-service"
* Both repositories should have separate cache entries

## Cache infrastructure should work
* Start the application with caching enabled
* Manually store data in cache
* Manually retrieve data from cache
* Retrieved data should match stored data

## Service should work without caching
* Start the application with caching disabled
* Get pull requests for repository "techronymsService"