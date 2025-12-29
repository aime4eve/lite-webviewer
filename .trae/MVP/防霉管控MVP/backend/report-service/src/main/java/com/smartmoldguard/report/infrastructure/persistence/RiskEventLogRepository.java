package com.smartmoldguard.report.infrastructure.persistence;

import com.smartmoldguard.report.domain.model.RiskEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RiskEventLogRepository extends JpaRepository<RiskEventLog, Long> {
    
    @Query("SELECT COUNT(r) FROM RiskEventLog r WHERE r.detectedAt >= :start AND r.detectedAt < :end")
    Integer countByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM RiskEventLog r WHERE r.riskLevel = :level AND r.detectedAt >= :start AND r.detectedAt < :end")
    Integer countByRiskLevelAndDateRange(String level, LocalDateTime start, LocalDateTime end);
}
