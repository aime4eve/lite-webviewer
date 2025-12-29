package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubscriptionE2ETest {

    private static final String BASE_URI = "http://localhost:8085";
    private static Long testUserId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        testUserId = System.currentTimeMillis(); // Unique User ID
    }

    @Test
    @Order(1)
    @DisplayName("1. Create Subscription")
    void testCreateSubscription() {
        String requestBody = String.format("""
            {
                "userId": %d,
                "planName": "PREMIUM_YEARLY"
            }
        """, testUserId);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/subscriptions/subscribe")
        .then()
            .statusCode(200)
            .body("userId", equalTo(testUserId))
            .body("planName", equalTo("PREMIUM_YEARLY"))
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Add Loyalty Points")
    void testAddPoints() {
        String requestBody = String.format("""
            {
                "userId": %d,
                "points": 100
            }
        """, testUserId);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/subscriptions/points/add")
        .then()
            .statusCode(200)
            .body("points", equalTo(100))
            .body("totalEarned", equalTo(100));
    }

    @Test
    @Order(3)
    @DisplayName("3. Verify Points History")
    void testGetPointsHistory() {
        given()
            .pathParam("userId", testUserId)
        .when()
            .get("/api/v1/subscriptions/{userId}/points/history")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].amount", equalTo(100))
            .body("[0].type", equalTo("EARN"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Check Active Subscription")
    void testGetActiveSubscription() {
        given()
            .pathParam("userId", testUserId)
        .when()
            .get("/api/v1/subscriptions/{userId}/active")
        .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"));
    }
}
