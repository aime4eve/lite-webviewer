package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

@Data
public class WorkOrderCreateRequest {
    private Long deviceId;
    private String type; // cleaning, repair
    private String description;
    private String assignee; // Optional
}
