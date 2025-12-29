package com.smartmoldguard.report.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_event_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long deviceId;
    
    private String riskLevel; // CRITICAL, WARNING
    
    private LocalDateTime detectedAt;
}
