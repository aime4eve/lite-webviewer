package com.smartmoldguard.device.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTelemetryEvent {
    private Long deviceId;
    private Double temperature;
    private Double humidity;
    private String location;
    private LocalDateTime timestamp;
}
