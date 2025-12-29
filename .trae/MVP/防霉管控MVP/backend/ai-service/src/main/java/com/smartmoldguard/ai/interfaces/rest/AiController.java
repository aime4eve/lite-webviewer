package com.smartmoldguard.ai.interfaces.rest;

import com.smartmoldguard.ai.domain.model.RiskAssessment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.smartmoldguard.ai.domain.model.DeviceHealthFingerprint;
import com.smartmoldguard.ai.domain.service.DeviceHealthService;
import com.smartmoldguard.ai.domain.service.RiskService;
import com.smartmoldguard.ai.interfaces.rest.dto.PredictionFeedbackRequest;
import com.smartmoldguard.ai.interfaces.rest.dto.PredictionFeedbackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "AI Service", description = "AI 预测与分析接口")
public class AiController {

    private final RiskService riskService;
    private final DeviceHealthService deviceHealthService;

    @GetMapping("/risk/{deviceId}/current")
    @Operation(summary = "获取当前风险评估")
    public ResponseEntity<RiskAssessment> getCurrentRisk(@PathVariable Long deviceId) {
        RiskAssessment risk = riskService.getLatestRisk(deviceId);
        if (risk == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(risk);
    }

    @GetMapping("/health-fingerprint/{deviceId}")
    @Operation(summary = "获取设备健康指纹")
    public ResponseEntity<DeviceHealthFingerprint> getHealthFingerprint(@PathVariable Long deviceId) {
        return ResponseEntity.ok(deviceHealthService.getHealthFingerprint(deviceId));
    }

    @PostMapping("/prediction-feedback")
    public ResponseEntity<PredictionFeedbackResponse> submitFeedback(@RequestBody PredictionFeedbackRequest request) {
        return ResponseEntity.ok(riskService.submitFeedback(request));
    }
}
