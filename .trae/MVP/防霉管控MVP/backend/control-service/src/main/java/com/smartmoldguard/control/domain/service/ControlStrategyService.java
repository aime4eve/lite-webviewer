package com.smartmoldguard.control.domain.service;

import com.smartmoldguard.control.domain.event.ControlCommandEvent;
import com.smartmoldguard.control.domain.event.RiskDetectedEvent;
import com.smartmoldguard.control.infrastructure.messaging.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ControlStrategyService {

    private final KafkaProducerService producerService;

    public void processRiskEvent(RiskDetectedEvent event) {
        log.info("Processing risk event for device {}: Level={}", event.getDeviceId(), event.getRiskLevel());

        // Check User Preference (Mock)
        // In real impl, fetch from repo. Here we assume Auto-Control is ENABLED.
        boolean autoControlEnabled = true; 
        if (!autoControlEnabled) {
            log.info("Auto control disabled for device {}", event.getDeviceId());
            return;
        }

        // Simple MVP Strategy:
        // CRITICAL -> Turn ON Heater + Fan
        // WARNING -> Turn ON Fan
        // SAFE -> Turn OFF Everything

        if ("CRITICAL".equals(event.getRiskLevel())) {
            sendCommand(event.getDeviceId(), "TURN_ON_HEATER", "Critical Risk Detected");
            sendCommand(event.getDeviceId(), "TURN_ON_FAN", "Critical Risk Detected");
        } else if ("WARNING".equals(event.getRiskLevel())) {
            sendCommand(event.getDeviceId(), "TURN_ON_FAN", "Warning Risk Detected");
            sendCommand(event.getDeviceId(), "TURN_OFF_HEATER", "Risk Reduced to Warning");
        } else {
            sendCommand(event.getDeviceId(), "TURN_OFF_FAN", "Risk Safe");
            sendCommand(event.getDeviceId(), "TURN_OFF_HEATER", "Risk Safe");
        }
    }

    private void sendCommand(Long deviceId, String type, String reason) {
        ControlCommandEvent command = ControlCommandEvent.builder()
                .commandId(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .commandType(type)
                .reason(reason)
                .timestamp(Instant.now())
                .build();
        
        producerService.sendControlCommand(command);
    }
}
