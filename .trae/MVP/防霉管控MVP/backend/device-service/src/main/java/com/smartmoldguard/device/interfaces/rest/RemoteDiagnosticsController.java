package com.smartmoldguard.device.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 远程诊断接口
 * 负责设备日志拉取与透传指令
 */
@RestController
@RequestMapping("/api/v1/diagnostics")
@Tag(name = "Remote Diagnostics", description = "远程诊断接口")
public class RemoteDiagnosticsController {

    /**
     * 拉取设备日志
     * @param deviceId 设备ID
     * @return 日志列表
     */
    @GetMapping("/logs")
    @Operation(summary = "拉取设备日志")
    public ResponseEntity<List<String>> getDeviceLogs(@RequestParam Long deviceId) {
        // Mock Logs (实际应从 InfluxDB 或设备端拉取)
        return ResponseEntity.ok(List.of(
                "2025-12-25 10:00:00 [INFO] Device started",
                "2025-12-25 10:05:00 [INFO] Connected to WiFi",
                "2025-12-25 10:10:00 [WARN] Humidity high"
        ));
    }

    /**
     * 透传指令
     * @param deviceId 设备ID
     * @param command 指令内容
     * @return 发送结果
     */
    @PostMapping("/command")
    @Operation(summary = "透传指令")
    public ResponseEntity<String> sendCommand(@RequestParam Long deviceId, @RequestBody String command) {
        // Mock command sending (实际应通过 MQTT 下发)
        return ResponseEntity.ok("Command sent: " + command);
    }
}
