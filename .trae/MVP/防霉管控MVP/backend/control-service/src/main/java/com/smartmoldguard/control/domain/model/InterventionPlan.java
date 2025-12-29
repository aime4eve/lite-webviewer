package com.smartmoldguard.control.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionPlan {
    private String planId;
    private Long deviceId;
    private String status; // ACTIVE, COMPLETED, FAILED
    private Instant startTime;
    private Instant expectedEndTime;
    private String strategyName; // e.g., "Fan Only", "Fan + Heater"
}
