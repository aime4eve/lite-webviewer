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
public class ControlCommandEvent {
    private String commandId;
    private Long deviceId;
    private String commandType; // TURN_ON_FAN, TURN_OFF_FAN, TURN_ON_HEATER, TURN_OFF_HEATER
    private String reason; // "High Risk Detected"
    private Instant timestamp;
}
