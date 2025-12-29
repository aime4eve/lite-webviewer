package com.smartmoldguard.subscription.interfaces.rest.dto;

import lombok.Data;

@Data
public class PointsRequest {
    private Long userId;
    private Integer points;
}
