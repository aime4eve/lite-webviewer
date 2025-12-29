package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

@Data
public class DeviceTelemetryRequest {
    private Double temperature;
    private Double humidity;
}
