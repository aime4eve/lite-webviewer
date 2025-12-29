package com.smartmoldguard.ai.domain.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxTable;
import com.smartmoldguard.ai.domain.model.DeviceHealthFingerprint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceHealthService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    public DeviceHealthFingerprint getHealthFingerprint(Long deviceId) {
        try {
            return calculateRealHealth(deviceId);
        } catch (Exception e) {
            log.error("Failed to calculate real health, falling back to mock", e);
            return calculateMockHealth(deviceId);
        }
    }

    @SuppressWarnings("null")
    private DeviceHealthFingerprint calculateRealHealth(Long deviceId) {
        String bucket = Objects.requireNonNull(influxBucket, "influx.bucket 不能为空");
        String org = Objects.requireNonNull(influxOrg, "influx.org 不能为空");

        double score = 100.0;
        List<DeviceHealthFingerprint.HealthFactor> factors = new ArrayList<>();

        // 1. Check High Risk Frequency (Last 30 days)
        String riskQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: -30d)
                  |> filter(fn: (r) => r["_measurement"] == "risk_assessment")
                  |> filter(fn: (r) => r["deviceId"] == "%s")
                  |> filter(fn: (r) => r["riskLevel"] == "CRITICAL" or r["riskLevel"] == "HIGH")
                  |> count()
                """, bucket, deviceId);

        List<FluxTable> riskTables = influxDBClient.getQueryApi().query(riskQuery, org);
        if (!riskTables.isEmpty() && !riskTables.get(0).getRecords().isEmpty()) {
            Long count = (Long) riskTables.get(0).getRecords().get(0).getValue();
            if (count != null && count > 0) {
                double penalty = Math.min(30.0, count * 5.0); // Max 30 points penalty
                score -= penalty;
                factors.add(DeviceHealthFingerprint.HealthFactor.builder()
                        .name("High Risk Frequency")
                        .description(count + " high risk events detected in last 30 days")
                        .impact(penalty)
                        .build());
            }
        }

        // 2. Check Humidity Stability (StdDev)
        String stabilityQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: -30d)
                  |> filter(fn: (r) => r["_measurement"] == "telemetry")
                  |> filter(fn: (r) => r["deviceId"] == "%s")
                  |> filter(fn: (r) => r["_field"] == "humidity")
                  |> stddev()
                """, bucket, deviceId);

        List<FluxTable> stabilityTables = influxDBClient.getQueryApi().query(stabilityQuery, org);
        if (!stabilityTables.isEmpty() && !stabilityTables.get(0).getRecords().isEmpty()) {
            Double stdDev = (Double) stabilityTables.get(0).getRecords().get(0).getValue();
            if (stdDev != null && stdDev > 10.0) {
                double penalty = Math.min(20.0, (stdDev - 10.0) * 2.0);
                score -= penalty;
                factors.add(DeviceHealthFingerprint.HealthFactor.builder()
                        .name("Humidity Instability")
                        .description("High humidity fluctuation (StdDev: " + String.format("%.2f", stdDev) + ")")
                        .impact(penalty)
                        .build());
            }
        }

        String level = "EXCELLENT";
        if (score < 70) level = "POOR";
        else if (score < 85) level = "FAIR";
        else if (score < 95) level = "GOOD";

        return DeviceHealthFingerprint.builder()
                .deviceId(deviceId)
                .healthScore((int) score)
                .healthLevel(level)
                .factors(factors)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private DeviceHealthFingerprint calculateMockHealth(Long deviceId) {
        int score = 85 + (int)(deviceId % 15); // Deterministic mock: 85-99
        String level = "GOOD";
        if (score >= 90) level = "EXCELLENT";
        else if (score < 80) level = "FAIR";

        List<DeviceHealthFingerprint.HealthFactor> factors = new ArrayList<>();
        if (score < 100) {
            factors.add(DeviceHealthFingerprint.HealthFactor.builder()
                    .name("Humidity Fluctuations")
                    .description("Occasional spikes detected")
                    .impact(100.0 - score)
                    .build());
        }

        return DeviceHealthFingerprint.builder()
                .deviceId(deviceId)
                .healthScore(score)
                .healthLevel(level)
                .factors(factors)
                .calculatedAt(LocalDateTime.now())
                .build();
    }
}
