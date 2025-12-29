package com.smartmoldguard.ai.interfaces.rest.dto;

import lombok.Data;

@Data
public class PredictionFeedbackRequest {
    private Long deviceId;
    private Integer rating; // 1-5
    private String comment;
    private String riskLevel; // safe/low/medium/high
}
