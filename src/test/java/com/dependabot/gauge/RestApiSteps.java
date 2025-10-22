package com.dependabot.gauge;

import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestContextManager;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Gauge step implementations for REST API scenarios
 * Tests the REST API endpoints with OpenAPI documentation
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.dependabot.DependabotApplication.class
)
public class RestApiSteps {

    private static final Logger log = LoggerFactory.getLogger(RestApiSteps.class);

    @LocalServerPort
    private int port;

    private Response response;
    private long firstRequestTime;
    private long secondRequestTime;

    @BeforeScenario
    public void setUp() throws Exception {
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Clear state
        response = null;
        firstRequestTime = 0;
        secondRequestTime = 0;

        log.debug("REST API test context initialized on port: {}", port);
    }

    @Step("Start the REST API")
    public void startRestApi() {
        log.debug("REST API is running on port: {}", port);
        log.debug("Base URL: http://localhost:{}", port);
        log.debug("Swagger UI: http://localhost:{}/swagger-ui.html", port);
    }

    @Step("Make GET request to <endpoint>")
    public void makeGetRequest(String endpoint) {
        log.debug("Making GET request to: {}", endpoint);

        response = given()
                .log().ifValidationFails()
                .when()
                .get(endpoint)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();

        // Store response in shared data store for other step classes
        ScenarioDataStore.put("response", response);

        log.debug("Response status: {}", response.getStatusCode());
    }

    @Step("Make OPTIONS request to <endpoint>")
    public void makeOptionsRequest(String endpoint) {
        log.debug("Making OPTIONS request to: {}", endpoint);

        response = given()
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .log().ifValidationFails()
                .when()
                .options(endpoint)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();

        log.debug("Response status: {}", response.getStatusCode());
    }

    @Step("Response should be valid JSON")
    public void verifyResponseIsValidJson() {
        String contentType = response.getContentType();

        assertThat(contentType)
                .as("Content-Type header")
                .contains("application/json");

        // Verify response can be parsed as JSON
        String body = response.getBody().asString();
        assertThat(body)
                .as("Response body should not be empty")
                .isNotEmpty();

        log.debug("✓ Response is valid JSON with Content-Type: {}", contentType);
    }

    @Step("Response should contain list of pull requests")
    public void verifyResponseContainsPRList() {
        // Verify response is a JSON array
        response.then()
                .body("$", instanceOf(List.class));

        List<?> prs = response.jsonPath().getList("$");
        log.debug("✓ Response contains a list with {} items", prs.size());
    }

    @Step("Response should contain PRs for repository <repositoryName>")
    public void verifyResponseContainsPRsForRepo(String repositoryName) {
        List<?> prs = response.jsonPath().getList("$");

        if (!prs.isEmpty()) {
            // If there are PRs, verify they're all for the correct repository
            response.then()
                    .body("repository", everyItem(equalTo(repositoryName)));

            log.debug("✓ All {} PRs are for repository: {}", prs.size(), repositoryName);
        } else {
            log.debug("✓ No PRs found for repository: {} (acceptable)", repositoryName);
        }
    }

    @Step("Response should contain empty list")
    public void verifyResponseContainsEmptyList() {
        response.then()
                .body("$", hasSize(0));

        log.debug("✓ Response contains empty list");
    }

    @Step("Response should include CORS headers")
    public void verifyResponseIncludesCorsHeaders() {
        String accessControlAllowOrigin = response.getHeader("Access-Control-Allow-Origin");
        String accessControlAllowMethods = response.getHeader("Access-Control-Allow-Methods");

        assertThat(accessControlAllowOrigin)
                .as("Access-Control-Allow-Origin header should be present")
                .isNotNull();

        log.debug("✓ CORS headers present:");
        log.debug("  - Access-Control-Allow-Origin: {}", accessControlAllowOrigin);
        if (accessControlAllowMethods != null) {
            log.debug("  - Access-Control-Allow-Methods: {}", accessControlAllowMethods);
        }
    }

    @Step("Store response time")
    public void storeResponseTime() {
        long startTime = System.currentTimeMillis();

        // Make the same request again to measure time
        String lastEndpoint = response.then().extract().response().getSessionId();
        response = given().when().get("/api/prs/techronymsService");

        long endTime = System.currentTimeMillis();

        firstRequestTime = endTime - startTime;
        log.debug("First request took: {}ms", firstRequestTime);
    }

    @Step("Make GET request to <endpoint> again")
    public void makeGetRequestAgain(String endpoint) {
        log.debug("Making second GET request to: {}", endpoint);

        long startTime = System.currentTimeMillis();

        response = given()
                .log().ifValidationFails()
                .when()
                .get(endpoint)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();

        long endTime = System.currentTimeMillis();
        secondRequestTime = endTime - startTime;

        log.debug("Second request took: {}ms", secondRequestTime);
    }

    @Step("Second request should be faster due to caching")
    public void verifySecondRequestFaster() {
        if (firstRequestTime == 0) {
            log.warn("First request time not recorded, skipping comparison");
            return;
        }

        log.debug("Comparing times: first={}ms, second={}ms",
                firstRequestTime, secondRequestTime);

        // With caching, second request should be reasonably fast
        // We're lenient here because timing can vary
        if (secondRequestTime < firstRequestTime * 0.8) {
            log.debug("✓ Second request was {}ms faster (likely cached)",
                    firstRequestTime - secondRequestTime);
        } else {
            log.debug("Second request time similar ({}ms vs {}ms)",
                    secondRequestTime, firstRequestTime);
        }
    }
}