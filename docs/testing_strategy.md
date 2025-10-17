# Testing Strategy

## Test Types

### Unit Tests (`./gradlew test`)
- Fast, isolated tests with mocked dependencies
- Test business logic without network calls
- Run on every commit

### Integration Tests (`./load-env-and-test.sh`)
- Test against real GitHub API
- Verify authentication and API integration
- Can run with or without Dependabot PRs present

## Test Repositories

### Current Setup
Tests run against your actual repositories:
- `techronymsService`
- `techronyms-user-service`
- `dependabot-pr-summariser`

Tests are **flexible** - they pass whether or not Dependabot PRs exist.

### Future: Dedicated Test Repositories (Optional)

When you want more control, create these test repos:

1. **dependabot-test-active**
    - Enable Dependabot
    - Has outdated dependencies
    - Always has 1-2 open Dependabot PRs

2. **dependabot-test-empty**
    - No dependencies / No PRs
    - Tests empty state handling

3. **dependabot-test-mixed**
    - Has both Dependabot and human-authored PRs
    - Tests filtering logic

Update `.env` to use test repos:
```bash
GITHUB_REPOS=dependabot-test-active,dependabot-test-empty,dependabot-test-mixed
```

## Running Tests
```bash
# All tests (flexible - pass with or without PRs)
./load-env-and-test.sh

# Only tests that don't require PRs
gauge run --tags "!requires-prs" specs

# Only tests that verify PR content (requires PRs to exist)
gauge run --tags "requires-prs" specs
```

## CI/CD

In CI, use GitHub Secrets for credentials and run all tests.
Tests will pass whether PRs exist or not.