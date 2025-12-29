package com.smartmoldguard.report.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmoldguard.report.domain.model.RiskEventLog;
import com.smartmoldguard.report.infrastructure.persistence.RiskEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final RiskEventLogRepository riskEventLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "risk-detected-topic", groupId = "report-service-group")
    @SuppressWarnings("null")
    public void consumeRiskDetectedEvent(String message) {
        log.info("Received Risk Event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            RiskEventLog eventLog = RiskEventLog.builder()
                    .deviceId(node.get("deviceId").asLong())
                    .riskLevel(node.get("riskLevel").asText())
                    .detectedAt(LocalDateTime.now()) // Use current time or parse from message
                    .build();
            
            riskEventLogRepository.save(Objects.requireNonNull(eventLog, "风险事件不能为空"));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse risk event", e);
        }
    }
}
