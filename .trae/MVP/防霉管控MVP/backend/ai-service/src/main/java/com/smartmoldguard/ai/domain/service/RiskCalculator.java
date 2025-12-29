package com.smartmoldguard.ai.domain.service;

import com.smartmoldguard.ai.domain.model.ClimateConfiguration;
import com.smartmoldguard.ai.domain.model.Microclimate;
import com.smartmoldguard.ai.domain.model.RiskAssessment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * 风险计算引擎
 * 基于规则和配置计算霉菌生长风险
 */
@Service
@RequiredArgsConstructor
public class RiskCalculator {

    private final ClimateConfigurationService climateConfigurationService;

    /**
     * 计算风险
     * @param microclimate 微气候数据
     * @param location 设备位置
     * @return 风险评估结果
     */
    public RiskAssessment calculateRisk(Microclimate microclimate, String location) {
        // 获取该位置的气候配置
        ClimateConfiguration config = climateConfigurationService.getParametersForLocation(location);

        double temp = microclimate.getTemperature();
        double humidity = microclimate.getHumidity();
        double score = 0.0;
        String level = "LOW";
        String recommendation = "No action needed";

        // 基于配置的阈值进行判断
        double humidityThreshold = config.getHumidityThreshold(); // e.g. 70.0
        double tempThreshold = config.getTempThreshold(); // e.g. 25.0
        double multiplier = config.getRiskFactorMultiplier(); // e.g. 1.0

        if (humidity > (humidityThreshold + 15.0)) {
            // 严重风险：湿度远超阈值
            score = 0.9 * multiplier;
            level = "CRITICAL";
            recommendation = "Turn on heater and fan immediately";
        } else if (humidity > humidityThreshold) {
            // 高风险：湿度超过阈值
            score = 0.7 * multiplier;
            level = "HIGH";
            recommendation = "Turn on fan";
        } else if (temp > tempThreshold && humidity > (humidityThreshold - 10.0)) {
            // 中等风险：高温且湿度较高
            score = 0.5 * multiplier;
            level = "MEDIUM";
            recommendation = "Monitor closely, consider ventilation";
        }

        // 确保分数在 0.0 - 1.0 之间
        if (score > 1.0) score = 1.0;
        if (score < 0.0) score = 0.0;

        return RiskAssessment.builder()
                .id(UUID.randomUUID().toString())
                .deviceId(microclimate.getDeviceId())
                .timestamp(Instant.now())
                .riskScore(score)
                .riskLevel(level)
                .recommendation(recommendation)
                .build();
    }
    
    // 保留旧方法签名以防其他地方调用，默认位置为null
    public RiskAssessment calculateRisk(Microclimate microclimate) {
        return calculateRisk(microclimate, null);
    }
}
