package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 告警统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmStatisticsDto {
    /**
     * 总告警数
     */
    private int total;

    /**
     * 高级告警数
     */
    private int high;

    /**
     * 中级告警数
     */
    private int medium;

    /**
     * 低级告警数
     */
    private int low;

    /**
     * 告警趋势
     */
    private List<AlarmTrendPoint> trend;
}
