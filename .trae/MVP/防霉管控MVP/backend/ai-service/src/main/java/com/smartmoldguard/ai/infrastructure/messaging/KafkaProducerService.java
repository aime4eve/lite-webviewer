package com.smartmoldguard.ai.infrastructure.messaging;

import com.smartmoldguard.ai.domain.event.PredictionFeedbackEvent;
import com.smartmoldguard.ai.domain.event.RiskDetectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRiskEvent(RiskDetectedEvent event) {
        try {
            kafkaTemplate.send("risk-detected-topic", event);
            System.out.println("Sent risk event: " + event);
        } catch (Exception e) {
            System.err.println("Failed to send risk event: " + e.getMessage());
        }
    }

    public void sendFeedbackEvent(PredictionFeedbackEvent event) {
        try {
            kafkaTemplate.send("prediction-feedback-topic", event);
            System.out.println("Sent feedback event: " + event);
        } catch (Exception e) {
            System.err.println("Failed to send feedback event: " + e.getMessage());
        }
    }
}
