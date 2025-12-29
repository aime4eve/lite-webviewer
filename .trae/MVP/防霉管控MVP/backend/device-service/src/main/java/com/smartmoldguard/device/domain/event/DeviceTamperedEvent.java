package com.smartmoldguard.device.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备防拆告警事件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTamperedEvent {
    private Long deviceId;
    private String macAddress;
    private String location;
    private LocalDateTime timestamp;
    private String alertType; // "tamper"
    private String status; // "unhandled"
}
