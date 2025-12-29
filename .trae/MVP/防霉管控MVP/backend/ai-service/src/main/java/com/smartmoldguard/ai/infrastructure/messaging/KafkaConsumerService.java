package com.smartmoldguard.ai.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmoldguard.ai.domain.event.DeviceTelemetryEvent;
import com.smartmoldguard.ai.domain.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final RiskService riskService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "device-telemetry-topic", groupId = "ai-service-group")
    public void consumeTelemetry(String message) {
        try {
            System.out.println("Received telemetry: " + message);
            DeviceTelemetryEvent event = objectMapper.readValue(message, DeviceTelemetryEvent.class);
            riskService.processTelemetry(event.getDeviceId(), event.getTemperature(), event.getHumidity(), event.getLocation());
        } catch (Exception e) {
            System.err.println("Failed to process telemetry: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
