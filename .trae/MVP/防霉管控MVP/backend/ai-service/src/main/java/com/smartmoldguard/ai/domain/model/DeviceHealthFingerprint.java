package com.smartmoldguard.ai.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备健康指纹
 * 描述设备的健康状况评分及维度
 */
@Data
@Builder
public class DeviceHealthFingerprint {
    private Long deviceId;
    private Integer healthScore; // 0-100
    private String healthLevel; // EXCELLENT, GOOD, FAIR, POOR
    private List<HealthFactor> factors;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    public static class HealthFactor {
        private String name;
        private String description;
        private Double impact; // Negative impact on score
    }
}
