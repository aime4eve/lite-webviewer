package com.smartmoldguard.control.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDetectedEvent {
    private String eventId;
    private Long deviceId;
    private Double riskScore;
    private String riskLevel; // SAFE, WARNING, CRITICAL
    private String recommendation;
    private Instant timestamp;
}
