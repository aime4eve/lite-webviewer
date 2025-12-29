package com.smartmoldguard.control.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/control/risk")
@Tag(name = "Risk Control", description = "风险控制接口")
public class RiskControlController {

    @GetMapping("/overview")
    @Operation(summary = "获取风险概览")
    public ResponseEntity<RiskOverviewDto> getRiskOverview() {
        // Mock Data
        return ResponseEntity.ok(RiskOverviewDto.builder()
                .averageRiskScore(0.45)
                .highRiskCount(2)
                .mediumRiskCount(5)
                .totalMonitoring(150)
                .build());
    }

    @GetMapping("/high-risk-rooms")
    @Operation(summary = "获取高风险房间")
    public ResponseEntity<List<HighRiskRoomDto>> getHighRiskRooms(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        // Mock Data
        return ResponseEntity.ok(List.of(
                HighRiskRoomDto.builder()
                        .id(101L)
                        .name("302室 - 主卧")
                        .riskLevel("CRITICAL")
                        .riskScore(0.95)
                        .currentTemp(28.0)
                        .currentHumidity(88.0)
                        .build(),
                HighRiskRoomDto.builder()
                        .id(102L)
                        .name("302室 - 地下室")
                        .riskLevel("HIGH")
                        .riskScore(0.75)
                        .currentTemp(22.0)
                        .currentHumidity(82.0)
                        .build()
        ));
    }

    @Data
    @Builder
    public static class RiskOverviewDto {
        private double averageRiskScore;
        private int highRiskCount;
        private int mediumRiskCount;
        private int totalMonitoring;
    }

    @Data
    @Builder
    public static class HighRiskRoomDto {
        private Long id;
        private String name;
        private String riskLevel;
        private double riskScore;
        private double currentTemp;
        private double currentHumidity;
    }
}
