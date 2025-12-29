package com.smartmoldguard.device.interfaces.rest;

import com.smartmoldguard.device.application.service.AlarmService;
import com.smartmoldguard.device.domain.model.Alarm;
import com.smartmoldguard.device.interfaces.rest.dto.AlarmDto;
import com.smartmoldguard.device.interfaces.rest.dto.AlarmStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用告警接口
 */
@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
@Tag(name = "General Alarm", description = "通用告警接口")
public class AlarmController {

    /**
     * 告警服务
     */
    private final AlarmService alarmService;

    /**
     * 查询告警
     * @param deviceId 设备ID (可选)
     * @param status 状态 (可选)
     * @return 告警列表
     */
    @GetMapping
    @Operation(summary = "查询告警")
    public ResponseEntity<List<AlarmDto>> getAlarms(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String status) {
        List<Alarm> alarms = alarmService.getAlarms(deviceId, status);
        List<AlarmDto> dtos = alarms.stream().map(alarm -> AlarmDto.builder()
                .id(alarm.getId())
                .deviceId(alarm.getDeviceId())
                .severity(alarm.getSeverity())
                .message(alarm.getMessage())
                .status(alarm.getStatus())
                .timestamp(alarm.getTimestamp())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 获取告警统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "告警统计")
    public ResponseEntity<AlarmStatisticsDto> getAlarmStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(alarmService.getStatistics(startDate, endDate));
    }

    /**
     * 确认告警
     * @param id 告警ID
     * @return Void
     */
    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认告警")
    public ResponseEntity<Void> confirmAlarm(@PathVariable Long id) {
        alarmService.confirmAlarm(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 清除告警
     * @param id 告警ID
     * @return Void
     */
    @PostMapping("/{id}/clear")
    @Operation(summary = "清除告警")
    public ResponseEntity<Void> clearAlarm(@PathVariable Long id) {
        alarmService.clearAlarm(id);
        return ResponseEntity.ok().build();
    }
}
