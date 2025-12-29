package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControlLoopE2ETest {

    private static final String DEVICE_SERVICE_URL = "http://localhost:8081";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:19092";
    private static final String CONTROL_COMMAND_TOPIC = "control-command-topic";
    
    private static Long testDeviceId;
    private static Consumer<String, String> kafkaConsumer;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = DEVICE_SERVICE_URL;

        // Setup Kafka Consumer to verify Control Commands
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "e2e-test-control-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Collections.singletonList(CONTROL_COMMAND_TOPIC));
    }

    @AfterAll
    static void tearDown() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Register Device for Control Loop")
    void registerDevice() {
        String sn = "CTRL-DEV-" + System.currentTimeMillis();
        
        String requestBody = String.format("""
            {
                "sn": "%s",
                "name": "E2E Test Dehumidifier",
                "location": "Bathroom",
                "description": "Created by ControlLoopE2ETest"
            }
            """, sn);

        testDeviceId = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/v1/devices/register")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath().getLong("id");
            
        System.out.println("Registered Device ID: " + testDeviceId);
    }

    @Test
    @Order(2)
    @DisplayName("2. Trigger Critical Risk (High Humidity)")
    void triggerCriticalRisk() {
        String telemetryBody = String.format("""
            {
                "temperature": 25.0,
                "humidity": 95.0
            }
            """, testDeviceId);

        given()
            .contentType(ContentType.JSON)
            .body(telemetryBody)
            .when()
            .post("/api/v1/devices/" + testDeviceId + "/telemetry")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("3. Verify Control Command Issued")
    void verifyControlCommand() {
        // Poll Kafka for Control Command
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500));
            boolean foundFan = false;
            boolean foundHeater = false;
            
            for (ConsumerRecord<String, String> record : records) {
                String value = record.value();
                System.out.println("Received Command: " + value);
                if (value.contains(String.valueOf(testDeviceId))) {
                    if (value.contains("TURN_ON_FAN")) foundFan = true;
                    if (value.contains("TURN_ON_HEATER")) foundHeater = true;
                }
            }

            assertTrue(foundFan, "未收到针对该设备的风扇开启指令");
            assertTrue(foundHeater, "未收到针对该设备的加热开启指令");
        });
    }
    
    // Removed redundant verifyControlCommandRobust

}
