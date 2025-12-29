package com.smartmoldguard.report.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_risk_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRiskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reportDate;
    
    private Integer totalRisksDetected;
    
    private Integer criticalRisks;
    
    private Integer warningRisks;
    
    private LocalDateTime generatedAt;
}
