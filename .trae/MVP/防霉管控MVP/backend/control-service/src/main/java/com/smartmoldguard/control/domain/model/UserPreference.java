package com.smartmoldguard.control.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
    private Long userId; // Or deviceId if per-device
    private Long deviceId;
    private boolean autoControlEnabled;
    private boolean quietMode; // If true, don't use loud fan at night
}
