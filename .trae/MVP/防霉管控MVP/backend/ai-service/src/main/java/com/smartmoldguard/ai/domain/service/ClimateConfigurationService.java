package com.smartmoldguard.ai.domain.service;

import com.smartmoldguard.ai.domain.model.ClimateConfiguration;
import com.smartmoldguard.ai.infrastructure.persistence.ClimateConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 气候带配置服务
 * 管理不同气候带的算法参数
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClimateConfigurationService {

    private final ClimateConfigurationRepository repository;

    /**
     * 获取所有气候带配置
     */
    public List<ClimateConfiguration> getAllConfigurations() {
        return repository.findAll();
    }

    /**
     * 根据代码获取配置
     */
    public Optional<ClimateConfiguration> getConfiguration(String zoneCode) {
        return repository.findByZoneCode(zoneCode);
    }

    /**
     * 创建或更新配置
     */
    @Transactional
    public ClimateConfiguration saveConfiguration(ClimateConfiguration config) {
        log.info("Saving climate configuration for zone: {}", config.getZoneCode());
        Optional<ClimateConfiguration> existing = repository.findByZoneCode(config.getZoneCode());
        if (existing.isPresent()) {
            ClimateConfiguration toUpdate = existing.get();
            toUpdate.setZoneName(config.getZoneName());
            toUpdate.setTempThreshold(config.getTempThreshold());
            toUpdate.setHumidityThreshold(config.getHumidityThreshold());
            toUpdate.setRiskFactorMultiplier(config.getRiskFactorMultiplier());
            toUpdate.setDescription(config.getDescription());
            return repository.save(toUpdate);
        }
        return repository.save(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfiguration(String zoneCode) {
        repository.findByZoneCode(zoneCode).ifPresent(repository::delete);
    }

    /**
     * 根据地理位置获取参数（模拟）
     * 实际场景可能需要根据经纬度调用地图服务获取位置信息，再映射到气候带
     * 这里简化为直接传入 zoneCode，如果不存在则返回默认
     */
    public ClimateConfiguration getParametersForLocation(String location) {
        // 简单映射逻辑，实际可扩展
        String zoneCode = mapLocationToZone(location);
        return repository.findByZoneCode(zoneCode)
                .orElseGet(this::getDefaultConfiguration);
    }

    private String mapLocationToZone(String location) {
        if (location == null) return "DEFAULT";
        if (location.contains("Guangdong") || location.contains("Shenzhen") || location.contains("Guangzhou")) {
            return "CN-SOUTH"; // 南方潮湿
        }
        if (location.contains("Beijing") || location.contains("North")) {
            return "CN-NORTH"; // 北方干燥
        }
        return "DEFAULT";
    }

    private ClimateConfiguration getDefaultConfiguration() {
        return repository.findByZoneCode("DEFAULT")
                .orElseGet(() -> {
                    ClimateConfiguration defaultConfig = ClimateConfiguration.builder()
                            .zoneCode("DEFAULT")
                            .zoneName("Standard Climate")
                            .tempThreshold(25.0)
                            .humidityThreshold(70.0)
                            .riskFactorMultiplier(1.0)
                            .description("Default configuration")
                            .build();
                    return repository.save(defaultConfig);
                });
    }
}
