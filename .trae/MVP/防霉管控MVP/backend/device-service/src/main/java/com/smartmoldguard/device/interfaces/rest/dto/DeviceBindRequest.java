package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

@Data
public class DeviceBindRequest {
    private String sn;
    private String name;
    private Long userId; // Assuming userId is passed for binding
}
