package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmDto {
    /**
     * 告警ID
     */
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 严重程度 (HIGH, MEDIUM, LOW)
     */
    private String severity;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 状态 (ACTIVE, CLEARED, CONFIRMED)
     */
    private String status;

    /**
     * 发生时间
     */
    private LocalDateTime timestamp;
}
