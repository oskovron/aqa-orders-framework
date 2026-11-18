package com.example.autotests.smoke;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import com.example.autotests.env.TestEnvironment;

class HealthSmokeTest {

    @Test
    void sutHealthIsUp() {
        given()
                .baseUri(TestEnvironment.sutBaseUri())
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }
}