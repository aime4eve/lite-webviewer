package com.smartmoldguard.ai.interfaces.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionFeedbackResponse {
    private Long id; // Mock ID or generated
    private Long deviceId;
    private Integer rating;
    private String status;
    private String message;
}
