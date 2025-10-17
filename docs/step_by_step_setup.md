# Step-by-Step Spring Boot Setup with TDD/BDD

## Phase 1: Initial Project Setup

### Step 1: Install Prerequisites

```bash
# Install Java 21
# Download from: https://adoptium.net/

# Install Gradle (if not already installed)
# macOS
brew install gradle

# Linux
sdk install gradle 8.5

# Windows
# Download from: https://gradle.org/install/

# Install Gauge
# macOS
brew install gauge

# Linux
curl -SsL https://downloads.gauge.org/stable | sh

# Windows
choco install gauge

# Verify installations
java -version    # Should show 21.x.x
gradle -v        # Should show 8.x
gauge version    # Should show gauge version
```

### Step 2: Create Project Directory

```bash
# Create project root
mkdir dependabot-service
cd dependabot-service

# Initialize as Git repository
git init

# Create .gitignore
cat > .gitignore << 'EOF'
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# IDE
.idea/
*.iml
.vscode/
*.code-workspace

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# Environment
.env
.env.local

# Gauge
reports/
logs/

# Spring Boot
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/
EOF
```

### Step 3: Create Gradle Configuration Files

Create these files in your project root:
- `build.gradle` - Main build configuration (from artifact above)
- `settings.gradle` - Project settings (from artifact above)
- `gradle.properties` - Gradle properties (from artifact above)

### Step 4: Create Directory Structure

```bash
# Create main source directories
mkdir -p src/main/java/com/dependabot
mkdir -p src/main/resources

# Create test directories
mkdir -p src/test/java/com/dependabot
mkdir -p src/test/resources

# Create Gauge directories
mkdir -p specs
mkdir -p src/test/java/com/dependabot/gauge
```

### Step 5: Verify Gradle Setup

```bash
# Download dependencies and verify build
./gradlew build

# Or on Windows
gradlew.bat build

# Run tests (should pass - no tests yet)
./gradlew test

# Check dependencies
./gradlew dependencies
```

### Step 6: Initialize Gauge

```bash
# Initialize Gauge project (if not using gradle task)
gauge init java

# This creates:
# - specs/ directory for specification files
# - env/ directory for environment configs
# - manifest.json for Gauge configuration
```

## Phase 2: Create Basic Application Structure

### Step 1: Create Main Application Class

Let's start with a failing test first!

Create `src/test/java/com/dependabot/DependabotApplicationTest.java`:

```java
package com.dependabot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DependabotApplicationTest {

    @Test
    void contextLoads() {
        // This test will fail until we create the main application class
        assertThat(true).isTrue();
    }
}
```

**Run the test** - it should fail because the application class doesn't exist:
```bash
./gradlew test
```

Now create `src/main/java/com/dependabot/DependabotApplication.java`:

```java
package com.dependabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DependabotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DependabotApplication.class, args);
    }
}
```

**Run the test again** - it should now pass:
```bash
./gradlew test
```

### Step 2: Create Basic Configuration

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: dependabot-service

logging:
  level:
    root: INFO
    com.dependabot: DEBUG
```

### Step 3: Verify Application Runs

```bash
# Run the application
./gradlew bootRun

# In another terminal, test the application is running
curl http://localhost:8080/actuator/health

# Stop with Ctrl+C
```

## Phase 3: Setup Testing Infrastructure

### Step 1: Create Test Base Classes

Create `src/test/java/com/dependabot/TestBase.java`:

```java
package com.dependabot;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for integration tests that need the full Spring context
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class TestBase {
    // Common test configuration will go here
}
```

### Step 2: Create First Gauge Specification

Create `specs/health_check.spec`:

```gauge
# Health Check API

## Application should be healthy
* Start the application
* Make a GET request to "/actuator/health"
* Response status code should be "200"
* Response should contain "UP"
```

Create `src/test/java/com/dependabot/gauge/HealthCheckSteps.java`:

```java
package com.dependabot.gauge;

import com.thoughtworks.gauge.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckSteps {

    @LocalServerPort
    private int port;

    private Response response;

    @Step("Start the application")
    public void startApplication() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Step("Make a GET request to <endpoint>")
    public void makeGetRequest(String endpoint) {
        response = given()
            .when()
            .get(endpoint);
    }

    @Step("Response status code should be <statusCode>")
    public void verifyStatusCode(String statusCode) {
        assertThat(response.getStatusCode())
            .isEqualTo(Integer.parseInt(statusCode));
    }

    @Step("Response should contain <text>")
    public void verifyResponseContains(String text) {
        assertThat(response.getBody().asString())
            .contains(text);
    }
}
```

### Step 3: Run Your First Gauge Test

```bash
# Run Gauge tests
./gradlew gaugeTest

# Or directly with Gauge
gauge run specs
```

## What We've Achieved

✅ Created a Spring Boot project with Gradle
✅ Set up JUnit 5 for unit testing
✅ Set up Gauge for BDD/integration testing
✅ Created a basic application that starts
✅ Verified testing infrastructure works

## Next Steps - Building Feature by Feature

Now we'll build each feature using TDD/BDD:

1. **Health Check Endpoint** (simple, good starting point)
2. **Configuration Management** (GitHub properties, cache config)
3. **GitHub Service** (core integration logic)
4. **Caching Layer** (Caffeine cache)
5. **REST API** (PR endpoints)
6. **Scheduler** (background jobs)

For each feature, we'll:
1. Write Gauge specification (BDD - what it should do)
2. Write failing unit tests (TDD - red)
3. Implement just enough code to pass (TDD - green)
4. Refactor (TDD - refactor)
5. Run Gauge tests to verify end-to-end behavior
