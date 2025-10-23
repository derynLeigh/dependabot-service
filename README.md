# Dependabot PR Service

A Spring Boot service for managing and summarizing Dependabot pull requests across multiple GitHub repositories.

## Prerequisites

- **Java 21** - [Download from Adoptium](https://adoptium.net/)
- **Gradle 8.10+** (included via wrapper)
- **Gauge** - For BDD testing
- **direnv** - For automatic environment variable loading
- **GitHub App credentials** - App ID, Installation ID, and Private Key

### Installing Prerequisites

```bash
# macOS
brew install gradle gauge direnv

# Linux (using SDKMAN for Gradle)
sdk install gradle 8.10
curl -SsL https://downloads.gauge.org/stable | sh

# Add direnv hook to your shell
# For Zsh (add to ~/.zshrc)
echo 'eval "$(direnv hook zsh)"' >> ~/.zshrc
source ~/.zshrc

# For Bash (add to ~/.bashrc)
echo 'eval "$(direnv hook bash)"' >> ~/.bashrc
source ~/.bashrc
```

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/derynLeigh/dependabot-service.git
cd dependabot-service
```

### 2. Configure Environment Variables

The project uses `direnv` to automatically load environment variables when you enter the project directory.

```bash
# Copy the environment template
cp .env.template .env.local

# Edit .env.local with your actual GitHub credentials
# You can use any editor - vim, nano, VSCode, etc.
nano .env.local
```

Fill in your values in `.env.local`:

```bash
GITHUB_APP_ID=your-app-id-here
GITHUB_INSTALLATION_ID=your-installation-id-here
GITHUB_OWNER=your-github-username-or-org
GITHUB_REPOS=repo1,repo2,repo3
GITHUB_PRIVATE_KEY="-----BEGIN RSA PRIVATE KEY-----
Your full private key here
including the BEGIN and END lines
-----END RSA PRIVATE KEY-----"
```

**Important:**
- Never commit `.env.local` - it contains secrets!
- The `.env.local` file is already in `.gitignore`

### 3. Allow direnv

```bash
# Allow direnv to load environment variables for this directory
direnv allow
```

You should see a message like:
```
direnv: loading ~/path/to/dependabot-service/.envrc
direnv: export +GITHUB_APP_ID +GITHUB_INSTALLATION_ID ...
```

Now whenever you `cd` into this directory, your environment variables will be automatically loaded!

## Running the Application

### Start the Application

```bash
# Start the Spring Boot application
./gradlew bootRun
```

The application will start on port **8081** by default.

### Verify It's Running

```bash
# Check health endpoint
curl http://localhost:8081/health

# Expected response:
# {"status":"UP","service":"dependabot-pr-service"}
```

## Running Tests

### Run Unit Tests

```bash
# Run all JUnit tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "com.dependabot.controller.HealthControllerTest"
```

### Run BDD/Integration Tests (Gauge)

```bash
# Run all Gauge specifications
./gradlew gaugeTest

# Run a specific spec file
./gradlew gaugeRun -Pspec=specs/health_check.spec

# Or use Gauge directly
gauge run specs
```

### Run All Tests

```bash
# Run both unit and integration tests
./gradlew test gaugeTest
```

## Building the Application

```bash
# Build without running tests
./gradlew build -x test

# Build with all tests
./gradlew build

# Create executable JAR
./gradlew bootJar

# The JAR will be created at: build/libs/dependabot-service.jar
```

## Test Key

The project uses a dedicated test RSA key for unit tests. If you need to regenerate it:
```bash
openssl genrsa -out src/test/resources/test-github-key.pem 2048
```

This key is for testing only and is ignored by GitGuardian via `.gitguardian.yaml`.

## Development Workflow

### Hot Reload with DevTools

The project includes Spring Boot DevTools for automatic restarts during development:

```bash
# Start with DevTools active
./gradlew bootRun

# Make changes to your code
# The application will automatically restart when you recompile
```

### Code Structure

```
dependabot-service/
├── src/
│   ├── main/
│   │   ├── java/com/dependabot/
│   │   │   ├── DependabotApplication.java
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── service/             # Business logic
│   │   │   └── model/               # Domain models
│   │   └── resources/
│   │       └── application.yml      # Application configuration
│   └── test/
│       ├── java/com/dependabot/
│       │   ├── gauge/               # Gauge step implementations
│       │   ├── controller/          # Controller tests
│       │   └── service/             # Service tests
│       └── resources/
│           └── application-test.yml # Test configuration
├── specs/                           # Gauge specifications (BDD)
├── build.gradle                     # Gradle build configuration
└── .envrc                          # direnv configuration
```

## Troubleshooting

### Environment Variables Not Loading

```bash
# Check if direnv is installed
direnv version

# Check if direnv hook is in your shell config
cat ~/.zshrc | grep direnv  # or ~/.bashrc for bash

# Re-allow the directory
direnv allow

# Check what variables are loaded
printenv | grep GITHUB
```

### Application Fails to Start

```bash
# Check if .env.local exists and has values
cat .env.local

# Check if port 8081 is already in use
lsof -i :8081

# Run with debug logging
./gradlew bootRun --debug
```

### Tests Failing

```bash
# Clean and rebuild
./gradlew clean build

# Check test reports
open build/reports/tests/test/index.html

# Run with stack traces
./gradlew test --stacktrace
```

### Private Key Issues

If you're having trouble with the multi-line private key in `.env.local`:

```bash
# Ensure the key is wrapped in double quotes
# Ensure it includes the BEGIN and END lines
# Keep all line breaks intact

# The format should be:
GITHUB_PRIVATE_KEY="-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEA...
...multiple lines...
...
-----END RSA PRIVATE KEY-----"
```

## Getting GitHub App Credentials

If you need to create a GitHub App:

1. Go to GitHub Settings → Developer settings → GitHub Apps
2. Click "New GitHub App"
3. Fill in the required details
4. Generate a private key (download the `.pem` file)
5. Install the app on your repositories
6. Note down:
    - App ID (from the app settings page)
    - Installation ID (from the installation URL or API)
    - Private Key (contents of the `.pem` file)

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Gauge Documentation](https://docs.gauge.org/)
- [GitHub Apps Documentation](https://docs.github.com/en/apps)
- [direnv Documentation](https://direnv.net/)

## Support

For issues or questions, please:
- Check the troubleshooting section above
- Review existing issues in the repository
- Create a new issue with detailed information about your problem

## Security Notes

⚠️ **Never commit the following files:**
- `.env.local` - Contains your secrets
- `*.pem` - Private key files
- Any file containing credentials

These files are already in `.gitignore`, but double-check before committing!