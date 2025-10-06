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