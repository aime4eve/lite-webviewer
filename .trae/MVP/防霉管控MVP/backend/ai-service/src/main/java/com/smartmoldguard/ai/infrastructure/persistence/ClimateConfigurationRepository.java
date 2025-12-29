package com.smartmoldguard.ai.infrastructure.persistence;

import com.smartmoldguard.ai.domain.model.ClimateConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 气候带配置仓储接口
 */
@Repository
public interface ClimateConfigurationRepository extends JpaRepository<ClimateConfiguration, Long> {
    Optional<ClimateConfiguration> findByZoneCode(String zoneCode);
}
