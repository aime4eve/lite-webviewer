package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Phase1FeaturesE2ETest {

    private static final Logger log = LoggerFactory.getLogger(Phase1FeaturesE2ETest.class);
    private static final String DEVICE_SERVICE_URL = "http://localhost:8081";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/device_db";
    private static final String DB_USER = "smg_admin";
    private static final String DB_PASSWORD = "smg_password_2025";

    private static Long testDeviceId;
    private static Long testWorkOrderId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = DEVICE_SERVICE_URL;
        // Register a device for testing
        String sn = "SN_PHASE1_" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = String.format("""
            {
                "sn": "%s",
                "name": "Phase1 Test Device",
                "location": "Test Lab Phase1",
                "description": "Device for Phase 1 Features"
            }
            """, sn);

        testDeviceId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/devices/register")
                .then()
                .statusCode(200)
                .extract().jsonPath().getLong("id");
        
        log.info("Registered Test Device ID: {}", testDeviceId);
    }

    @Test
    @Order(1)
    @DisplayName("1. Auto-Provisioning APIs")
    void testProvisioning() {
        // Info
        given()
                .get("/api/v1/provisioning/info")
                .then()
                .statusCode(200)
                .body("ssidPrefix", startsWith("SmartMoldGuard_"))
                .body("discoveryPort", is(1900));

        // Key Exchange
        String keyExchangeReq = """
                {
                    "devicePublicKey": "MOCK_DEVICE_KEY",
                    "deviceSn": "SN_MOCK"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(keyExchangeReq)
                .when()
                .post("/api/v1/provisioning/exchange-keys")
                .then()
                .statusCode(200)
                .body("sessionToken", notNullValue())
                .body("serverPublicKey", startsWith("MOCK_SERVER_PUBLIC_KEY_"));
    }

    @Test
    @Order(2)
    @DisplayName("2. WorkOrder Management (Create, Assign, Forward, Batch)")
    void testWorkOrderManagement() {
        // Create WorkOrder
        String createReq = String.format("""
                {
                    "deviceId": %d,
                    "type": "cleaning",
                    "description": "Weekly cleaning"
                }
                """, testDeviceId);
        
        testWorkOrderId = given()
                .contentType(ContentType.JSON)
                .body(createReq)
                .when()
                .post("/api/v1/work-orders")
                .then()
                .statusCode(200)
                .body("status", equalTo("pending"))
                .extract().jsonPath().getLong("id");

        // Assign
        given()
                .post("/api/v1/work-orders/" + testWorkOrderId + "/assign?assignee=user1")
                .then()
                .statusCode(200)
                .body("status", equalTo("assigned"))
                .body("assignee", equalTo("user1"));

        // Forward
        String forwardReq = """
                {
                    "toUser": "user2",
                    "reason": "Busy"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(forwardReq)
                .when()
                .post("/api/v1/work-orders/" + testWorkOrderId + "/forward")
                .then()
                .statusCode(200)
                .body("assignee", equalTo("user2"))
                .body("description", containsString("[Forward] From user1 to user2: Busy"));

        // Batch Assign
        // Create another work order
        Long id2 = given().contentType(ContentType.JSON).body(createReq).post("/api/v1/work-orders").jsonPath().getLong("id");
        
        String batchReq = String.format("""
                {
                    "workOrderIds": [%d, %d],
                    "assignee": "user3"
                }
                """, testWorkOrderId, id2);
        
        given()
                .contentType(ContentType.JSON)
                .body(batchReq)
                .when()
                .post("/api/v1/work-orders/batch-assign")
                .then()
                .statusCode(200);

        // Verify Batch Assign
        given()
                .get("/api/v1/work-orders/assigned?assignee=user3")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(3)
    @DisplayName("3. Remote Diagnostics")
    void testRemoteDiagnostics() {
        // Logs
        given()
                .get("/api/v1/diagnostics/logs?deviceId=" + testDeviceId)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        // Command
        given()
                .body("reboot")
                .post("/api/v1/diagnostics/command?deviceId=" + testDeviceId)
                .then()
                .statusCode(200)
                .body(containsString("Command sent: reboot"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Alarm Management")
    void testAlarmManagement() throws Exception {
        // Create a mock alarm directly in DB for testing
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO alarms (device_id, severity, message, status, timestamp, created_at, updated_at) " +
                    "VALUES (?, 'HIGH', 'Test Alarm', 'ACTIVE', NOW(), NOW(), NOW()) RETURNING id");
            stmt.setLong(1, testDeviceId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Long alarmId = rs.getLong("id");
            
            // Query
            given()
                    .get("/api/v1/alarms?deviceId=" + testDeviceId)
                    .then()
                    .statusCode(200)
                    .body("[0].id", equalTo(alarmId.intValue()))
                    .body("[0].message", equalTo("Test Alarm"));

            // Statistics
            given()
                    .get("/api/v1/alarms/statistics")
                    .then()
                    .statusCode(200)
                    .body("total", greaterThanOrEqualTo(1));

            // Confirm
            given()
                    .post("/api/v1/alarms/" + alarmId + "/confirm")
                    .then()
                    .statusCode(200);

            // Verify Confirm
             given()
                    .get("/api/v1/alarms?deviceId=" + testDeviceId)
                    .then()
                    .statusCode(200)
                    .body("[0].status", equalTo("CONFIRMED"));

             // Clear
            given()
                    .post("/api/v1/alarms/" + alarmId + "/clear")
                    .then()
                    .statusCode(200);
        }
    }
}
