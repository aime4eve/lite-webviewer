package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警趋势点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmTrendPoint {
    private String date;
    private int count;
}
