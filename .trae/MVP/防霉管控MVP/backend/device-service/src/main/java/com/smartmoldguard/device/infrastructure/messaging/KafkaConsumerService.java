package com.smartmoldguard.device.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmoldguard.device.domain.event.ControlCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "control-command-topic", groupId = "device-service-group")
    public void consumeControlCommand(String message) {
        try {
            log.info("Received control command: {}", message);
            ControlCommandEvent event = objectMapper.readValue(message, ControlCommandEvent.class);
            // TODO: Implement actual device control logic (e.g., call device API, update shadow)
            log.info("Executing command {} for device {}: {}", event.getCommandType(), event.getDeviceId(), event.getReason());
        } catch (Exception e) {
            log.error("Error processing control command", e);
        }
    }
}
