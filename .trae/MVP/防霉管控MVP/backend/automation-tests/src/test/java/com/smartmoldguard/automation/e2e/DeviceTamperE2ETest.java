package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeviceTamperE2ETest {

    private static final Logger log = LoggerFactory.getLogger(DeviceTamperE2ETest.class);
    private static final String DEVICE_SERVICE_URL = "http://localhost:8081";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:19092"; // External port
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/device_db";
    private static final String DB_USER = "smg_admin";
    private static final String DB_PASSWORD = "smg_password_2025";
    private static final String TAMPER_TOPIC = "device-tampered-topic";

    private static String testDeviceSn;
    private static Long testDeviceId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = DEVICE_SERVICE_URL;
        testDeviceSn = "SN_E2E_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @Order(1)
    @DisplayName("1. Infrastructure Health Check")
    void checkInfrastructure() {
        // Check Device Service Port (Simple Connectivity)
        try (java.net.Socket socket = new java.net.Socket("localhost", 8081)) {
            assertThat(socket.isConnected()).isTrue();
        } catch (Exception e) {
            Assertions.fail("Device Service not reachable on port 8081");
        }

        // Check DB Connection
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            assertThat(conn.isValid(2)).isTrue();
        } catch (SQLException e) {
            Assertions.fail("Database connection failed: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Device Registration")
    void registerDevice() {
        String requestBody = String.format("""
            {
                "sn": "%s",
                "name": "E2E Test Device",
                "location": "Test Lab",
                "description": "Automated Testing Device"
            }
            """, testDeviceSn);

        // API Call
        String response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/devices/register")
                .then()
                .statusCode(200)
                .extract().body().asString();

        log.info("Register Response: {}", response);

        // Verify DB
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM devices WHERE mac_address = ?");
            stmt.setString(1, testDeviceSn);
            ResultSet rs = stmt.executeQuery();
            assertThat(rs.next()).isTrue();
            testDeviceId = rs.getLong("id");
            log.info("Registered Device ID: {}", testDeviceId);
        } catch (SQLException e) {
            Assertions.fail("DB Verification failed: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Simulate Tamper & Verify Business Loop")
    void simulateTamperAndVerify() throws Exception {
        assertThat(testDeviceId).isNotNull();

        // Start Kafka Consumer asynchronously
        AtomicBoolean kafkaEventReceived = new AtomicBoolean(false);
        CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "e2e-test-group-" + UUID.randomUUID());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList(TAMPER_TOPIC));
                // Poll for 10 seconds
                long endTime = System.currentTimeMillis() + 10000;
                while (System.currentTimeMillis() < endTime) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        if (record.value().contains(testDeviceSn) || record.value().contains(String.valueOf(testDeviceId))) {
                            log.info("Received Kafka Event: {}", record.value());
                            kafkaEventReceived.set(true);
                            return;
                        }
                    }
                }
            }
        });

        // Give consumer a moment to subscribe
        Thread.sleep(2000);

        // Trigger API
        given()
                .post("/api/v1/devices/" + testDeviceId + "/simulate-tamper")
                .then()
                .statusCode(200);

        // Verify Kafka (Wait up to 10s)
        await().atMost(10, TimeUnit.SECONDS).untilTrue(kafkaEventReceived);

        // Verify DB - Work Order
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM work_orders WHERE device_id = ? AND type = 'tamper'");
                stmt.setLong(1, testDeviceId);
                ResultSet rs = stmt.executeQuery();
                assertThat(rs.next()).isTrue();
            }
        });

        // Verify DB - Asset Compensation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM asset_compensations WHERE device_id = ?");
                stmt.setLong(1, testDeviceId);
                ResultSet rs = stmt.executeQuery();
                assertThat(rs.next()).isTrue();
            }
        });

        // Verify DB - Alarm Created
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM alarms WHERE device_id = ? AND severity = 'HIGH'");
                stmt.setLong(1, testDeviceId);
                ResultSet rs = stmt.executeQuery();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("status")).isEqualTo("ACTIVE");
            }
        });
    }
}