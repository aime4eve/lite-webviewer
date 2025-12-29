package com.smartmoldguard.control.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmoldguard.control.domain.event.RiskDetectedEvent;
import com.smartmoldguard.control.domain.service.ControlStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ControlStrategyService strategyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "risk-detected-topic", groupId = "control-service-group")
    public void consumeRiskEvent(String message) {
        try {
            log.info("Received risk event: {}", message);
            RiskDetectedEvent event = objectMapper.readValue(message, RiskDetectedEvent.class);
            strategyService.processRiskEvent(event);
        } catch (Exception e) {
            log.error("Error processing risk event", e);
        }
    }
}
