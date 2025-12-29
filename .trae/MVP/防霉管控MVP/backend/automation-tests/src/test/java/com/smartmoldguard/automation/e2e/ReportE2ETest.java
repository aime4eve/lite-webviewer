package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReportE2ETest {

    private static final String BASE_URI = "http://localhost:8087";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @Order(1)
    @DisplayName("1. Trigger Daily Report Generation")
    void testTriggerReport() {
        // Trigger report for today
        String date = LocalDate.now().toString();

        given()
            .param("date", date)
        .when()
            .post("/api/v1/reports/generate/daily")
        .then()
            .statusCode(200)
            .body("reportDate", equalTo(date));
    }

    @Test
    @Order(2)
    @DisplayName("2. Get Daily Reports")
    void testGetReports() {
        given()
        .when()
            .get("/api/v1/reports/daily")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    @DisplayName("3. Export Report to PDF")
    void testExportPdf() {
        String date = LocalDate.now().toString();

        given()
            .pathParam("date", date)
        .when()
            .get("/api/v1/reports/daily/{date}/export/pdf")
        .then()
            .statusCode(200)
            .contentType("application/pdf");
    }
}
