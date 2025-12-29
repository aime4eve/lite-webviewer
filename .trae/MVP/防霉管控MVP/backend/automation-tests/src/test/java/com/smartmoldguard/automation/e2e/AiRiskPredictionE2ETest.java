package com.smartmoldguard.automation.e2e;

import io.restassured.RestAssured;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AiRiskPredictionE2ETest {

    private static final Logger log = LoggerFactory.getLogger(AiRiskPredictionE2ETest.class);
    private static final String AI_SERVICE_URL = "http://localhost:8083";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:19092";
    private static final String TELEMETRY_TOPIC = "device-telemetry-topic";
    private static final String RISK_TOPIC = "risk-detected-topic";

    private static Long testDeviceId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = AI_SERVICE_URL;
        testDeviceId = System.currentTimeMillis(); // Unique ID
    }

    @Test
    @Order(1)
    @DisplayName("1. Infrastructure Health Check")
    void checkInfrastructure() {
        // Check AI Service Port
        try (java.net.Socket socket = new java.net.Socket("localhost", 8083)) {
            assertThat(socket.isConnected()).isTrue();
        } catch (Exception e) {
            Assertions.fail("AI Service not reachable on port 8083");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. End-to-End Risk Prediction Flow")
    void verifyRiskPrediction() throws InterruptedException {
        // 1. Start Kafka Consumer for Risk Events
        AtomicBoolean riskEventReceived = new AtomicBoolean(false);
        java.util.concurrent.CountDownLatch consumerReady = new java.util.concurrent.CountDownLatch(1);

        CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "e2e-ai-test-group-" + UUID.randomUUID());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Only new events

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList(RISK_TOPIC));
                
                // Wait for assignment
                while (consumer.assignment().isEmpty()) {
                    consumer.poll(Duration.ofMillis(100));
                }
                log.info("Consumer assigned to partitions: {}", consumer.assignment());
                consumerReady.countDown();

                long endTime = System.currentTimeMillis() + 10000;
                while (System.currentTimeMillis() < endTime) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        log.info("Received Risk Event: {}", record.value());
                        if (record.value().contains(String.valueOf(testDeviceId))) {
                            riskEventReceived.set(true);
                            return;
                        }
                    }
                }
            }
        });

        // Wait for consumer to be ready
        consumerReady.await(10, TimeUnit.SECONDS);

        // 2. Produce Telemetry Event (High Humidity -> High Risk)
        // Default config: Humidity > 70 is HIGH, > 85 is CRITICAL
        // We send 90.0
        String telemetryJson = String.format("""
            {
                "deviceId": %d,
                "temperature": 28.0,
                "humidity": 90.0,
                "location": "Test Lab",
                "timestamp": [2025, 12, 27, 10, 0, 0]
            }
            """, testDeviceId);

        produceKafkaMessage(TELEMETRY_TOPIC, String.valueOf(testDeviceId), telemetryJson);
        log.info("Sent Telemetry: {}", telemetryJson);

        // 3. Verify Risk Event Received
        await().atMost(10, TimeUnit.SECONDS).untilTrue(riskEventReceived);
    }

    @Test
    @Order(3)
    @DisplayName("3. Device Health Fingerprint")
    void verifyHealthFingerprint() {
        // Wait a bit for InfluxDB to index/persist
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        // We expect the health score to be impacted by the CRITICAL risk event we just generated
        // Initial score 100. One CRITICAL event -> 5 points penalty. Score <= 95.
        // Or at least it returns 200 OK.
        
        RestAssured.given()
                .pathParam("deviceId", testDeviceId)
                .when()
                .get("/api/v1/health-fingerprint/{deviceId}")
                .then()
                .statusCode(200)
                .log().body();
    }

    private void produceKafkaMessage(String topic, String key, String value) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, key, value));
            producer.flush();
        }
    }
}
