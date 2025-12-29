package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeviceApiE2ETest {

    private static final Logger log = LoggerFactory.getLogger(DeviceApiE2ETest.class);
    private static final String DEVICE_SERVICE_URL = "http://localhost:8081";
    
    private static String testDeviceSn;
    private static Long testDeviceId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = DEVICE_SERVICE_URL;
        testDeviceSn = "SN_API_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @Order(1)
    @DisplayName("1. Register Device")
    void registerDevice() {
        String requestBody = String.format("""
            {
                "sn": "%s",
                "name": "API Test Device",
                "location": "API Lab",
                "description": "API Testing Device"
            }
            """, testDeviceSn);

        // Extract as Number to avoid casting issues between Integer/Long
        Number id = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/devices/register")
                .then()
                .statusCode(200)
                .body("name", equalTo("API Test Device"))
                .extract().path("id");
                
        testDeviceId = id.longValue();
        log.info("Registered Device ID: {}", testDeviceId);
    }

    @Test
    @Order(2)
    @DisplayName("2. Get Device List")
    void getDeviceList() {
        given()
            .when()
            .get("/api/v1/devices")
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("find { it.id == " + testDeviceId + " }.name", equalTo("API Test Device"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Get Device Detail")
    void getDeviceDetail() {
        given()
            .when()
            .get("/api/v1/devices/" + testDeviceId)
            .then()
            .statusCode(200)
            .body("id", equalTo(testDeviceId.intValue()))
            .body("macAddress", equalTo(testDeviceSn));
    }

    @Test
    @Order(4)
    @DisplayName("4. Update Device")
    void updateDevice() {
        String updateBody = """
            {
                "name": "Updated Device Name",
                "location": "Updated Location"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
            .when()
            .put("/api/v1/devices/" + testDeviceId)
            .then()
            .statusCode(200);

        // Verify Update
        given()
            .when()
            .get("/api/v1/devices/" + testDeviceId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Device Name"))
            .body("location", equalTo("Updated Location"));
    }
}
