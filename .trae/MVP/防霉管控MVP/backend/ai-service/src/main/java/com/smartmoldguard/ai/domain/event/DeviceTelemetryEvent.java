package com.smartmoldguard.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTelemetryEvent {
    private Long deviceId;
    private Double temperature;
    private Double humidity;
    private String location;
    private LocalDateTime timestamp;
}
