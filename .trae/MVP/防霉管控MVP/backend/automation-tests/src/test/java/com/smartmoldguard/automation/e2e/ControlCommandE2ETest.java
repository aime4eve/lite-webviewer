package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControlCommandE2ETest {

    private static final String BASE_URI = "http://localhost:8084";
    private static Long testDeviceId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        testDeviceId = System.currentTimeMillis();
    }

    @Test
    @Order(1)
    @DisplayName("1. Send Manual Command")
    void testSendCommand() {
        String requestBody = String.format("""
            {
                "deviceId": %d,
                "commandType": "TURN_ON_FAN",
                "parameters": {"speed": "HIGH"}
            }
        """, testDeviceId);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/control/commands/send")
        .then()
            .statusCode(200)
            .body("deviceId", equalTo(testDeviceId))
            .body("commandType", equalTo("TURN_ON_FAN"))
            .body("status", equalTo("SENT"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Get Command History")
    void testGetCommandHistory() {
        given()
            .pathParam("deviceId", testDeviceId)
        .when()
            .get("/api/v1/control/commands/history/{deviceId}")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].commandType", equalTo("TURN_ON_FAN"));
    }
}
