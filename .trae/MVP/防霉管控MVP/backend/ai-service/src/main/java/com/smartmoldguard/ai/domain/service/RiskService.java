package com.smartmoldguard.ai.domain.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.write.Point;
import com.influxdb.client.domain.WritePrecision;
import com.smartmoldguard.ai.domain.event.PredictionFeedbackEvent;
import com.smartmoldguard.ai.domain.event.RiskDetectedEvent;
import com.smartmoldguard.ai.domain.model.Microclimate;
import com.smartmoldguard.ai.domain.model.RiskAssessment;
import com.smartmoldguard.ai.infrastructure.messaging.KafkaProducerService;
import com.smartmoldguard.ai.interfaces.rest.dto.PredictionFeedbackRequest;
import com.smartmoldguard.ai.interfaces.rest.dto.PredictionFeedbackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskCalculator riskCalculator;
    private final KafkaProducerService kafkaProducerService;
    private final InfluxDBClient influxDBClient;
    
    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;
    
    // In-memory store for latest risk per device
    private final Map<Long, RiskAssessment> latestRiskMap = new ConcurrentHashMap<>();

    public void processTelemetry(Long deviceId, Double temp, Double humidity, String location) {
        Microclimate microclimate = Microclimate.builder()
                .deviceId(deviceId)
                .temperature(temp)
                .humidity(humidity)
                .timestamp(Instant.now())
                .build();
        
        // 1. Record Telemetry to InfluxDB
        recordTelemetry(microclimate);

        // 2. Calculate Risk
        RiskAssessment risk = riskCalculator.calculateRisk(microclimate, location);
        
        // 3. Record Risk to InfluxDB
        recordRisk(risk);
        
        // Update latest risk
        latestRiskMap.put(deviceId, risk);

        // If High Risk, Emit Event
        if (risk.getRiskScore() > 0.6) {
            RiskDetectedEvent riskEvent = RiskDetectedEvent.builder()
                    .eventId(risk.getId())
                    .deviceId(risk.getDeviceId())
                    .riskScore(risk.getRiskScore())
                    .riskLevel(risk.getRiskLevel())
                    .recommendation(risk.getRecommendation())
                    .timestamp(risk.getTimestamp())
                    .build();
            
            kafkaProducerService.sendRiskEvent(riskEvent);
        }
    }

    public RiskAssessment getLatestRisk(Long deviceId) {
        return latestRiskMap.get(deviceId);
    }
    
    public PredictionFeedbackResponse submitFeedback(PredictionFeedbackRequest request) {
        log.info("Received prediction feedback for device {}: rating={}, level={}", 
                request.getDeviceId(), request.getRating(), request.getRiskLevel());
        
        // Record feedback to InfluxDB for analytics
        try {
            Point point = Point.measurement("prediction_feedback")
                    .addTag("deviceId", String.valueOf(request.getDeviceId()))
                    .addTag("riskLevel", request.getRiskLevel())
                    .addField("rating", request.getRating())
                    .addField("comment", request.getComment() != null ? request.getComment() : "")
                    .time(Instant.now(), WritePrecision.MS);

            String bucket = Objects.requireNonNull(influxBucket, "influx.bucket 不能为空");
            String org = Objects.requireNonNull(influxOrg, "influx.org 不能为空");
            influxDBClient.getWriteApiBlocking().writePoint(bucket, org, point);
        } catch (Exception e) {
            log.error("Failed to write feedback to InfluxDB", e);
            // Don't fail the request just because analytics write failed
        }

        // Trigger Model Optimization (Mock) via Event
        PredictionFeedbackEvent feedbackEvent = PredictionFeedbackEvent.builder()
                .deviceId(request.getDeviceId())
                .rating(request.getRating())
                .comment(request.getComment())
                .riskLevel(request.getRiskLevel())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        kafkaProducerService.sendFeedbackEvent(feedbackEvent);

        return PredictionFeedbackResponse.builder()
                .id(System.currentTimeMillis()) // Mock ID
                .deviceId(request.getDeviceId())
                .rating(request.getRating())
                .status("submitted")
                .message("Feedback received successfully")
                .build();
    }

    private void recordTelemetry(Microclimate m) {
        try {
            Point point = Point.measurement("telemetry")
                    .addTag("deviceId", String.valueOf(m.getDeviceId()))
                    .addField("temperature", m.getTemperature())
                    .addField("humidity", m.getHumidity())
                    .time(m.getTimestamp(), WritePrecision.MS);

            String bucket = Objects.requireNonNull(influxBucket, "influx.bucket 不能为空");
            String org = Objects.requireNonNull(influxOrg, "influx.org 不能为空");
            influxDBClient.getWriteApiBlocking().writePoint(bucket, org, point);
        } catch (Exception e) {
            log.error("Failed to write telemetry to InfluxDB", e);
        }
    }

    private void recordRisk(RiskAssessment r) {
        try {
            Point point = Point.measurement("risk_assessment")
                    .addTag("deviceId", String.valueOf(r.getDeviceId()))
                    .addTag("riskLevel", r.getRiskLevel())
                    .addField("riskScore", r.getRiskScore())
                    .time(r.getTimestamp(), WritePrecision.MS);

            String bucket = Objects.requireNonNull(influxBucket, "influx.bucket 不能为空");
            String org = Objects.requireNonNull(influxOrg, "influx.org 不能为空");
            influxDBClient.getWriteApiBlocking().writePoint(bucket, org, point);
        } catch (Exception e) {
            log.error("Failed to write risk to InfluxDB", e);
        }
    }
}
