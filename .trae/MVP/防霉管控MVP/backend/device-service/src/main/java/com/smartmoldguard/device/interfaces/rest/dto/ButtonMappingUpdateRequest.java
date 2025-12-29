package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class ButtonMappingUpdateRequest {
    private List<ButtonMappingDto> mappings;

    @Data
    public static class ButtonMappingDto {
        private Integer switchPosition;
        private String deviceType;
        private String deviceName;
        private String icon;
    }
}
