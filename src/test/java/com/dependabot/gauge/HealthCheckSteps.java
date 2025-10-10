package com.dependabot.gauge;

import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestContextManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Gauge step implementations for Health Check API scenarios
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.dependabot.DependabotApplication.class
)
public class HealthCheckSteps {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckSteps.class);

    @LocalServerPort
    private int port;

    private Response response;

    private TestContextManager testContextManager;

    @BeforeScenario
    public void setUp() throws Exception {
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        log.debug("Spring context initialized. Port: {}", port);
    }

    @Step("Start the application on a random port")
    public void startApplication() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        log.debug("Application started on port: {}", port);
    }

    @Step("Make a GET request to <endpoint>")
    public void makeGetRequest(String endpoint) {
        response = given()
                .log().ifValidationFails()
                .when()
                .get(endpoint)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();

        log.debug("Response: {}", response.getBody().asString());
    }

    @Step("Make a GET request to <endpoint> without authentication")
    public void makeGetRequestWithoutAuth(String endpoint) {
        makeGetRequest(endpoint);
    }

    @Step("Response status code should be <statusCode>")
    public void verifyStatusCode(String statusCode) {
        int expectedCode = Integer.parseInt(statusCode);
        assertThat(response.getStatusCode())
                .as("Response status code")
                .isEqualTo(expectedCode);
    }

    @Step("Response content type should be <contentType>")
    public void verifyContentType(String contentType) {
        assertThat(response.getContentType())
                .as("Response content type")
                .contains(contentType);
    }

    @Step("Response should contain field <fieldName> with value <fieldValue>")
    public void verifyResponseField(String fieldName, String fieldValue) {
        response.then()
                .body(fieldName, equalTo(fieldValue));
    }
}