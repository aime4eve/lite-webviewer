package com.smartmoldguard.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Microclimate {
    private Long deviceId;
    private Instant timestamp;
    private double temperature;
    private double humidity;
}