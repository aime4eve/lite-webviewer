package com.smartmoldguard.ai.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 气候带配置实体
 * 用于存储不同气候带的算法参数
 */
@Entity
@Table(name = "climate_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClimateConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 气候带代码 (e.g., CN-SOUTH, CN-NORTH)
     */
    @Column(unique = true, nullable = false)
    private String zoneCode;

    /**
     * 气候带名称
     */
    private String zoneName;

    /**
     * 霉菌生长温度阈值 (摄氏度)
     */
    private Double tempThreshold;

    /**
     * 霉菌生长湿度阈值 (%)
     */
    private Double humidityThreshold;

    /**
     * 风险系数乘数
     */
    private Double riskFactorMultiplier;

    /**
     * 描述
     */
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
