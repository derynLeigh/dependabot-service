# PHASE 3
## Set Up Testing Infrastructure
### Step 1: Create Test Base Classes
Create `src/test/java/com/dependabot/TestBase.java`:
```
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
### Step 2: Create the First Gauge Specification
Create `specs/health_check.spec`
```
# Health Check API

## Application should be healthy
* Start the application
* Make a GET request to "/actuator/health"
* Response status code should be "200"
* Response should contain "UP"
```

Create `src/test/java/com/dependabot/gauge/HealthCheckSteps.java`:
```
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
``````
### Step 3: Run Your First Gauge Test
**Run Gauge tests:**

`./gradlew gaugeTest`

**Or directly with Gauge:**

`gauge run specs`