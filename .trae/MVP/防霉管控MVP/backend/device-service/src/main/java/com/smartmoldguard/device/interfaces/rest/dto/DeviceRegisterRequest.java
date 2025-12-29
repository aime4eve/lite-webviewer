package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

@Data
public class DeviceRegisterRequest {
    private String sn;
    private String name;
    private String location;
    private String description;
}
