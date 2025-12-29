package com.smartmoldguard.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionFeedbackEvent {
    private Long deviceId;
    private Integer rating;
    private String comment;
    private String riskLevel; // The user perceived risk level
    private LocalDateTime timestamp;
}
